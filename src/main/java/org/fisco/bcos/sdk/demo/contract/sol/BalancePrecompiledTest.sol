pragma solidity >=0.6.0 <0.8.0;

contract BalancePrecompiled {
    function getBalance(address account) public view returns (uint256) {}

    function addBalance(address account, uint256 amount) public {}

    function subBalance(address account, uint256 amount) public {}

    function transfer(address from, address to, uint256 amount) public {}

    function registerCaller(address account) public {}

    function unregisterCaller(address account) public {}

    function listCaller() public view returns (address[] memory) {}
}


contract BalancePrecompiledTest {
    event Caller(address account);

    BalancePrecompiled private balancePrecompiled = BalancePrecompiled(0x0000000000000000000000000000000000001011);

    modifier onlyBalanceCaller() {
        require(isBalanceCaller(msg.sender), "only balance caller can call");
        _;
    }

    constructor() public {
        balancePrecompiled.registerCaller(address(this));
        balancePrecompiled.registerCaller(msg.sender);
    }

    function isBalanceCaller(address account) public view returns (bool) {
        address[] memory list = balancePrecompiled.listCaller();
        return contains(list, account);
    }

    function contains(address[] memory list, address account) internal pure returns (bool) {
        for (uint256 i = 0; i < list.length; i++) {
            if (list[i] == account) {
                return true;
            }
        }
        return false;
    }

    function testRegisterAndUnregisterCaller() public onlyBalanceCaller {
        require(isBalanceCaller(address(this)), "should be balance caller");

        // double register should revert
        try balancePrecompiled.registerCaller(address(this)) {
            revert("should revert");
        } catch (bytes memory reason) {
        }

        // register
        address fakeAddr = address(block.timestamp);
        balancePrecompiled.registerCaller(fakeAddr);
        require(isBalanceCaller(fakeAddr), "should be balance caller");
    }

    function testClearBalanceCaller() public onlyBalanceCaller {
        address[] memory list = balancePrecompiled.listCaller();
        for (uint256 i = 0; i < list.length; i++) {
            if (list[i] == msg.sender || list[i] == address(this)) {
                continue;
            }
            balancePrecompiled.unregisterCaller(list[i]);
            require(!isBalanceCaller(list[i]), "should not be balance caller");
        }
        require(balancePrecompiled.listCaller().length == 2, "should only have 2 balance caller");
    }

    function testRegisterManyCaller() public onlyBalanceCaller returns(address[] memory){
        for (uint256 i = 0; i < 100; i++) {
            address fakeAddr = address(block.timestamp + i);
            balancePrecompiled.registerCaller(fakeAddr);
            require(isBalanceCaller(fakeAddr), "should be balance caller");
        }

        return balancePrecompiled.listCaller();
    }

    function testAddBalance() public onlyBalanceCaller {
        address user = address(block.timestamp);
        balancePrecompiled.addBalance(user, uint256(-1));

        // must revert if add 1
        try balancePrecompiled.addBalance(user, 1) {
            revert("should revert");
        } catch (bytes memory reason) {
        }

        address user2 = address(block.timestamp + 1);
        for (uint256 i = 0; i < 100; i++) {
            uint256 balance = balancePrecompiled.getBalance(user2);
            balancePrecompiled.addBalance(user2, 1);
            require(balancePrecompiled.getBalance(user2) == balance + 1, "balance should be increased by 1");
        }

        // test overflow
        try balancePrecompiled.addBalance(user2, uint256(-1)) {
            revert("should revert");
        } catch (bytes memory reason) {
        }
    }

    function testSubBalance() public onlyBalanceCaller {
        address user = address(block.timestamp);
        balancePrecompiled.addBalance(user, 100);
        balancePrecompiled.subBalance(user, 1);
        require(balancePrecompiled.getBalance(user) == 99, "balance should be decreased by 1");

        // must revert if sub 1
        try balancePrecompiled.subBalance(user, 100) {
            revert("should revert");
        } catch (bytes memory reason) {
        }

        // test overflow
        try balancePrecompiled.subBalance(user, uint256(-1)) {
            revert("should revert");
        } catch (bytes memory reason) {
        }

        // can sub to 0
        balancePrecompiled.subBalance(user, 99);
        require(balancePrecompiled.getBalance(user) == 0, "balance should be 0");
    }

    function testTransferBalance() public onlyBalanceCaller {
        address A = address(block.timestamp);
        address B = address(block.timestamp + 1);

        balancePrecompiled.addBalance(A, 100);
        balancePrecompiled.transfer(A, B, 100);
        require(balancePrecompiled.getBalance(A) == 0, "balance should be 0");
        require(balancePrecompiled.getBalance(B) == 100, "balance should be 100");

        balancePrecompiled.subBalance(B, 100);
        require(balancePrecompiled.getBalance(B) == 0, "balance should be 0");

        // test edge case
        balancePrecompiled.addBalance(A, 1);
        balancePrecompiled.addBalance(B, uint256(-2));
        balancePrecompiled.transfer(A, B, 1);
        require(balancePrecompiled.getBalance(A) == 0, "balance should be 0");
        require(balancePrecompiled.getBalance(B) == uint256(-1), "balance should be uint256(-1)");
    }

    function check() public onlyBalanceCaller {
        //testRegisterAndUnregisterCaller();
        //testRegisterManyCaller();
        testClearBalanceCaller();

        testAddBalance();
        testSubBalance();
        //testTransferBalance();
    }

}