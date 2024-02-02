// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract BalanceBank {

    // Use transfer method to withdraw an amount of money and for updating automatically the balance
    function transfer(address _to, uint256 _value) public {
        payable(_to).transfer(_value);
    }

    // Getter smart contract Balance
    function getSmartContractBalance() external view returns(uint256) {
        return address(this).balance;
    }

}