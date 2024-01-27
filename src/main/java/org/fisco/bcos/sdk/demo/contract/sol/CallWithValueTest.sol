pragma solidity >=0.6.0 <0.8.0;

contract CallWithValueTest {
    HelloWorld public hello;
    constructor () public {
        hello = new HelloWorld();
    }

    function callPrecompiledContract(address _precompiledContract, uint256 withValue) public {
        // 调用预编译合约的ECRecover函数
        bytes memory data = abi.encodeWithSignature("ecrecover(bytes32,uint8,bytes32,bytes32)", bytes32(0), uint8(27), bytes32(0), bytes32(0));
        (bool success, ) = _precompiledContract.call{value: withValue, gas: 200000}(data);
        require(success, "Call to precompiled contract failed");
    }

    // 0x0000000000000000000000000000000000000001
    function callPrecompiledContract(address _precompiledContract) public {
        // 调用预编译合约的ECRecover函数
        bytes memory data = abi.encodeWithSignature("ecrecover(bytes32,uint8,bytes32,bytes32)", bytes32(0), uint8(27), bytes32(0), bytes32(0));
        (bool success, ) = _precompiledContract.call(data);
        require(success, "Call to precompiled contract failed");
    }

    function callPrecompiledContract() public returns (address){
        // 调用预编译合约的ECRecover函数
        address precompiledContract = address(0x1);
        bytes memory data = abi.encode(
            bytes32(0xaa0f7414b7f8648410f9818df3a1f43419d5c30313f430712033937ae57854c8),
            uint8(28),
            bytes32(0xacd0d6c91242e514655815073f5f0e9aed671f68a4ed3e3e9d693095779f704b),
            bytes32(0x01932751f4431c3b4c9d6fb1c826d138ee155ea72ac9013d66929f6a265386b4));
        (bool success, bytes memory result) = precompiledContract.call(data);
        require(success, "Call to precompiled contract failed");
        return bytesToAddress(result);
    }


    function callRecover() public returns (address){

        bytes32 hash =  bytes32(0xaa0f7414b7f8648410f9818df3a1f43419d5c30313f430712033937ae57854c8);
        uint8 v = uint8(28);
        bytes32 r = bytes32(0xacd0d6c91242e514655815073f5f0e9aed671f68a4ed3e3e9d693095779f704b);
        bytes32 s = bytes32(0x01932751f4431c3b4c9d6fb1c826d138ee155ea72ac9013d66929f6a265386b4);

        address addr = ecrecover(hash, v, r, s);
        require(addr == address(0x6DA0599583855F1618B380f6782c0c5C25CB96Ec), "ecrecover failed");
        return addr;
    }


    function testCallHelloWorld(uint256 withValue) public payable {
        hello.set{value: withValue}("Hello, World!");
    }

    function transfer(address payable to, uint256 amount) public payable {
        to.transfer(amount);
    }

    function setHelloWorld(address addr) public payable {
        hello =  HelloWorld(addr);
    }


    function checkPrecompiledRange() public {
        callPrecompiledContract(address(0x1), 1);
    }

    function check() public {
        require(callRecover() == address(0x6DA0599583855F1618B380f6782c0c5C25CB96Ec), "callRecover failed");
    }

    function bytesToAddress(bytes memory bys) private pure returns (address addr) {
        assembly {
            addr := mload(add(bys, 20))
        }
    }
}


contract HelloWorld {
    string name;

    constructor() public {
        name = "Hello, World!";
    }

    function get() public view returns (string memory) {
        return name;
    }

    function set(string memory n) payable public {
        name = n;
    }
}