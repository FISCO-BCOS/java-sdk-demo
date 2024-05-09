pragma solidity >=0.6.0 <0.8.12;

contract StaticCall {
    HelloWorld helloWorld = new HelloWorld();
    event Result(bytes);
    event Result(uint);

    modifier initIfNot() {
        if (address(helloWorld) == address(0)) {
            helloWorld = new HelloWorld();
        }
        _;
    }

    function staticCallASM(address target, bytes memory data)
    internal
    view
    returns (bool result)
    {
        assembly {
            result := staticcall(gas(), target, add(data, 0x20), mload(data), mload(0x40), 0)
        }
        return result;
    }

    function staticCallUintASM(address target, bytes memory data)
    internal
    view
    returns (bool, uint)
    {
        uint ret;
        bool result;
        assembly {
            let size := 0x20
            let free := mload(0x40)
            result := staticcall(gas(), target, add(data, 0x20), mload(data), free, size)
            ret := mload(free)
        }
        return (result, ret);
    }

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

    function testASMOk() public returns(uint){
        bytes memory data = abi.encodeWithSignature("getNum()");
        bool ok = staticCallASM(address(helloWorld), data);
        require(ok);
        uint ret;
        (ok, ret) = staticCallUintASM(address(helloWorld), data);
        require(ok);
        require(ret == 100);
        emit Result(ret);
        return ret;
    }

    function testASMFailed() public {
        bytes memory data = abi.encodeWithSignature("setNum(uint256)", 222);
        bool ok = staticCallASM(address(helloWorld), data);
        require(!ok);
    }

    function testASMUintFailed() public {
        bytes memory data = abi.encodeWithSignature("setNum(uint256)", 222);
        (bool ok, uint ret) = staticCallUintASM(address(helloWorld), data);
        require(!ok);
    }

    function testEmptyAddr() public {
        (bool ok, bytes memory result) = address(0x10016666666).staticcall(abi.encodeWithSignature("get()"));
        require(ok, "addr not exist but must return ok to be the same as eth");
        require(result.length == 0, "result must be empty");
    }

    function check() public initIfNot {
        (bool ok, bytes memory result) = address(helloWorld).staticcall(abi.encodeWithSignature("get()"));
        require(ok);

        (ok, result) = address(helloWorld).staticcall(abi.encodeWithSignature("set(string)", "aaa"));
        require(!ok, "staticcall a state write function must return not ok");

        testEmptyAddr();

        testASMOk();
        testASMFailed();
        testASMUintFailed();
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
    uint num = 100;

    constructor() public {
        name = "Hello, World!";
    }

    function get() public view returns (string memory) {
        return name;
    }

    function set(string memory n) public {
        name = n;
    }

    function getNum() public view returns (uint) {
        return num;
    }

    function setNum(uint n) public {
        num = n;
    }
}