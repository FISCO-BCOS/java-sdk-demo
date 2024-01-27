pragma solidity >=0.6.0 <0.8.0;

contract DelegateCallDest {
    int public value = 0;
    address public sender;
    address public myAddress;

    event Info(string, address, address);

    constructor() public {
        myAddress = address(this);
    }

    function add() public returns(bytes memory) {
        sender = msg.sender;
        value += 2;
        return "2";
    }

    function callMyAddress() public {
        bool success;
        bytes memory ret;
        (success, ret) = myAddress.call(abi.encodeWithSignature("recordSender()"));
    }

    function mustRevert() public {
        require(false, "DelegateCallDest revert");
    }

    function callMyAddressRevert() public returns(bytes memory) {
        try DelegateCall(myAddress).mustRevert() {
            return "no catch";
        } catch (bytes memory reason) {
            return reason;
        }
    }


    function emitInfo() public {
        emit Info("Dest, this, msg.sender", address(this), msg.sender);
    }

    fallback() external {
        value += 1000;
    }
}

contract DelegateAtConstruct {
    constructor(address addr) public {
        addr.delegatecall(abi.encodeWithSignature("add()"));
    }
    /*
        function value() public returns(bytes memory) {
            (bool ok, bytes memory result) = addr.delegatecall(abi.encodeWithSignature("value()"));
            return result;
        }
        */
}

contract DelegateCall {
    int public value = 0;
    address public sender;
    address public myAddress;
    uint myCodeSize;
    bytes32 myCodeHash;
    address public delegateDest;
    address public latestSender;
    address public latestOrigin;

    event Info(string, address, address);
    event InfoBytes(string, bytes, bytes32);

    constructor() public {
        myAddress = address(this);
        delegateDest = address(new DelegateCallDest());
    }

    function add() public returns(bytes memory) {
        sender = msg.sender;
        value += 1;
        return "1";
    }

    function recordSender() public {
        latestSender = msg.sender;
        latestOrigin = tx.origin;
    }

    function testFailed() public {
        int v = value;
        address addr = address(0x1001);
        (bool ok, bytes memory result) = dCall(addr, "add()");
        require(ok, "addr not exist but must return ok to be the same as eth");
        require(v == value, "testFailed value must no change");
    }

    function testSuccess() public{
        int v = value;
        (bool ok, bytes memory result) = dCall(delegateDest, "add()");
        require(ok, "testSuccess must ok");
        require(v + 2 == value, "testSuccess value must +2");
    }

    function testFallback() public returns(bytes memory){
        int v = value;
        (bool ok, bytes memory result) = dCall(delegateDest, "triggerFallback()");
        require(v + 1000 == value, "testFallback value must +1000");
    }

    function testCallInDelegateCall() public {
        dCall(delegateDest, "callMyAddress()");
        require(address(this) == latestSender, "callInDelegateCall's msg.sender should be this contract");
        require(tx.origin == latestOrigin, "callInDelegateCall's msg.sender should be this contract");
    }

    function emitInfo() public {
        emit Info("Base, this, msg.sender", address(this), msg.sender);
    }

    function testEventInDelegateCall() public {
        emitInfo();
        dCall(delegateDest, "emitInfo()");
        //delegateDest.call(abi.encodeWithSignature("emitInfo()"));
        emitInfo();
    }

    function testDelegateCallSender() public {
        dCall(delegateDest, "add()");
        require(msg.sender == sender, "delegatecall's sender must be msg.sender");
    }

    function testCallcodeSender() public {
        address addr = delegateDest;
        bytes memory payload = abi.encodeWithSignature("add()");
        assembly {
            let success := callcode(gas(), addr, 0, add(payload, 0x20), mload(payload), 0, 0)
        }

        require(address(this) == sender, "callcode's sender must be this address");
    }

    function testDelegateCallAtConstruct() public returns(address) {
        DelegateAtConstruct test = new DelegateAtConstruct(delegateDest);
        return address(test);
    }

    function mustRevert() public {
        require(false, "Base revert");
    }

    function checkBytes(bytes memory origin, bytes32 hash, bytes32 smHash) public pure returns(bool) {
        return keccak256(origin) == hash || keccak256(origin) == smHash;
    }

    function testCatchInDelegateCallThrow() public {
        (bool ok, bytes memory result) = dCall(delegateDest, "callMyAddressRevert()");
        require(ok, "testCatchInDelegateCallThrow must not ok");
        emit InfoBytes("delegatecall result", result, keccak256(result));

        // check the same as remix
        require(checkBytes(result, bytes32(0x4dd31d082b4aaf0e899854bf1871e7f70e3794b3ec764ad291ab64cbdff7910d),
            bytes32(0xc770ee3912bb8b54fdeb59371079323aac0cca9ca0355dfddc07d5ee38c8f35b)), "testCatchInDelegateCallThrow checkBytes failed");

        try this.mustRevert() {
            emit Info("no catch", address(this), msg.sender);
            require(false, "must catch revert");
        } catch (bytes memory reason) {
            emit InfoBytes("this call result", reason, keccak256(reason));
            // check the same as remix
            require(checkBytes(reason, bytes32(0x0e58f8cab8bcea7dbf8463f931105075f2375b58db9353a6a1bc982fa9e84acf),
                bytes32(0x340af06566b98237072abe7f5c4c5a7c39059c64f09ea1de31622cb74f3e3be5)), "testCatchInDelegateCallThrow this call result checkBytes failed");
        }
    }


    function dCall(address addr, string memory func) private returns(bool, bytes memory) {
        return addr.delegatecall(abi.encodeWithSignature(func));
    }

    function codesizeAt(address addr) public returns(uint){
        uint len;
        assembly { len := extcodesize(addr) }
        return len;
    }

    function codehashAt(address addr) public returns(bytes32){
        bytes32 codeHash;
        assembly { codeHash := extcodehash(addr) }
        return codeHash;
    }

    function check() public {
        testFailed();
        testSuccess();
        testFallback();
        testCallInDelegateCall();
        testDelegateCallSender();
        testCallcodeSender();
        testEventInDelegateCall();
        testCatchInDelegateCallThrow();
    }

}
