pragma solidity ^0.8.11;

contract StaticCall {
    HelloWorld helloWorld = new HelloWorld();
    event Result(bytes);

    function getAddress() public view returns (address) {
        return address(helloWorld);
    }

    function testOk() public returns(bytes memory){
        (bool ok, bytes memory result) = address(helloWorld).staticcall(abi.encodeWithSignature("get()"));
        require(ok);
        emit Result(result);
        return result;
    }

    function testFailed() public {
        (bool ok, bytes memory result) = address(helloWorld).staticcall(abi.encodeWithSignature("set(string)", "aaa"));
        require(ok, "staticcall must call view/pure function");
    }

    function check() public {
        (bool ok, bytes memory result) = address(helloWorld).staticcall(abi.encodeWithSignature("get()"));
        require(ok);

        (ok, result) = address(helloWorld).staticcall(abi.encodeWithSignature("set(string)", "aaa"));
        require(!ok, "staticcall a state write function must return not ok");
    }

    function get() public view returns (string memory) {
        return helloWorld.get();
    }

    function set(string memory n) public {
        helloWorld.set(n);
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

    function set(string memory n) public {
        name = n;
    }
}