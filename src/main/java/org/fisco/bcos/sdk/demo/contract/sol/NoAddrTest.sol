// SPDX-License-Identifier: UNLICENSED
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

contract NoAddrTest {
    BalancePrecompiled balancePrecompiled = BalancePrecompiled(0x0000000000000000000000000000000000666666);

    function getBalance(address account) public returns (uint256) {
        return balancePrecompiled.getBalance(account);
    }

    function addBalance(address account, uint256 amount) public {
        balancePrecompiled.addBalance(account, amount);
    }
    function subBalance(address account, uint256 amount) public {
        balancePrecompiled.subBalance(account, amount);
    }
    function transfer(address from, address to, uint256 amount) public {
        balancePrecompiled.transfer(from, to, amount);
    }
    function registerCaller(address account) public {
        balancePrecompiled.registerCaller(account);
    }
    function unregisterCaller(address account) public {
        balancePrecompiled.unregisterCaller(account);
    }
    function listCaller() public returns (address[] memory) {
        return balancePrecompiled.listCaller();
    }
    function test() public {
        try balancePrecompiled.listCaller() {

        } catch(bytes memory reason) {

        }
    }
}
