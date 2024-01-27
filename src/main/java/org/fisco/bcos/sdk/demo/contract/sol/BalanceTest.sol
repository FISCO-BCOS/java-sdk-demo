pragma solidity >=0.6.0 <0.8.0;

contract BalanceTest {
    event Transfer(address indexed from, address indexed to, uint256 value);
    event Balance(uint256 value);
    event Info(string info);
    event Info(uint id, string info);
    event Info(address addr, string info);

    modifier mustHasBalance() {
        require(getSelfBalance() >= 0, "balance not enough");
        _;
    }

    function getBalance(address addr) public view returns(uint256) {
        return addr.balance;
    }

    function getSelfBalance() public view returns(uint256) {
        return address(this).balance;
    }

    function transfer(address payable to, uint256 amount) public payable {
        uint256 balanceBefore = getBalance(to);
        uint256 balanceBeforeSelf = getSelfBalance();
        to.transfer(amount);
        uint256 balanceAfter = getBalance(to);
        uint256 balanceAfterSelf = getSelfBalance();
        //require(balanceAfter - balanceBefore == amount, "balance should be increased by amount");
        //require(balanceBeforeSelf - balanceAfterSelf == amount, "self balance should be decreased by amount");
        emit Transfer(address(this), to, amount);
    }

    function testSelfBalance() public {
        require(getSelfBalance() != 0, "self balance should not be 0");
        require(getBalance(address(this)) == getSelfBalance(), "self balance should be 0");
    }

    function receiveBalance() public payable {
        require(msg.value > 0, "msg.value should be greater than 0");
        emit Balance(msg.value);
    }

    function testReceiveBalance() public payable {
        // must revert
        try this.receiveBalance() {
            revert("should revert");
        } catch (bytes memory reason) {
        }

        this.receiveBalance{value: 1}();
    }

    function notPayable() public {
        emit Info("Into notPayable");
    }

    function testCallNotPayableWithValue() public payable {
        // use call function to call notPayable
        (bool success,  bytes memory ret) = address(this).call{value: 1}(abi.encodeWithSignature("notPayable()"));
        require(!success, "call notPayable with value must failed");
    }

    function testDeployWithValue() public payable returns(address){
        // must success
        address successAddr = address(new ContractWithPayableConstructor{value: 1}());
        require(successAddr != address(0x0), "deploy failed");

        return successAddr;
    }

    function testTransferBalanceToPrecompiled() public mustHasBalance {
        // should be the same as eth
        address payable[22] memory okAddresses = [address(0x0), address(0x2), address(0x3), address(0x4), address(0x5),
            address(0x6), address(0xa), address(0xb), address(0xc), address(0xd), address(0xe), address(0xf),
            address(0x10), address(0x11), address(0x12), address(0x13), address(0x14), address(0x15),
            address(0x16), address(0x17), address(0x18), address(0x19)];

        address payable[4] memory errorAddresses = [address(0x1), address(0x7), address(0x8), address(0x9)];

        // foreach okAddresses
        for (uint256 i = 0; i < okAddresses.length; i++) {
            address payable addr = okAddresses[i];
            try this.transfer(addr, 1) {
                emit Info(addr, "success");
            } catch (bytes memory reason) {
                emit Info(addr, "false");
                revert(string(abi.encodePacked("should success, but failed, reason: ", reason)));
            }
        }

        return;
        // foreach errorAddresses
        for (uint256 i = 0; i < errorAddresses.length; i++) {
            address payable addr = errorAddresses[i];
            try this.transfer(addr, 1) {
                emit Info(addr, "success");
                revert("should revert");
            } catch (bytes memory reason) {
                emit Info(addr, "false");
            }
        }
    }


    // must call by admin
    function check() public mustHasBalance {
        testSelfBalance();
        testReceiveBalance();
        testCallNotPayableWithValue();
        testDeployWithValue();
        //testDeployNotPayableWithValue()
        //testTransferBalanceToPrecompiled();
        /*
testTransferBalance();
testTransferBalanceByCall();
testTransferBalanceByDelegateCall();
testTransferBalanceByCallCode();
testTransferBalanceByStaticCall();

testTransferBalanceBack();

testSelfdestruct();
testSelfdestructToSelf();
testSelfdestructToPrecompiled();
testTransferBalanceAfterSelfdestruct();
testSelfdestructAfterTransferBalance();
testSelfdestructAfterSelfdestruct();
*/
    }
}

contract ContractWithPayableConstructor {
    event Balance(uint256);
    string name;

    constructor() public payable {
        emit Balance(address(this).balance);
        name = "Hello, World!";
    }

    function get() public view returns (string memory) {
        return name;
    }

    function set(string memory n) public {
        name = n;
    }
}

contract ContractWithNonPayableConstructor {
    event Balance(uint256);
    string name;

    constructor() public {
        emit Balance(address(this).balance);
        name = "Hello, World!";
    }

    function get() public view returns (string memory) {
        return name;
    }

    function set(string memory n) public {
        name = n;
    }
}

