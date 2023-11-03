pragma solidity>=0.6.10 <0.8.20;


contract DelegateCallDest {
    int public value = 0;
    address public sender;
    address public myAddress;

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

    fallback() external {
        value += 1000;
    }
}

contract DelegateCall {
    int public value = 0;
    address public sender;
    address public myAddress;
    uint myCodeSize;
    bytes32 myCodeHash;
    address public delegateDest;
    address public latestSender;


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
    }

    function testFailed() public {
        int v = value;
        address addr = address(0x1001);
        (bool ok, bytes memory result) = dCall(addr, "add()");
        require(!ok, "testFailed must not ok");
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
    }

}
