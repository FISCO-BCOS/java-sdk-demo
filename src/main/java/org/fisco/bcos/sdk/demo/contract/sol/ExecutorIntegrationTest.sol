pragma solidity ^0.8.0;

import "./SafeMath.sol";
contract ExecutorIntegrationTest {
    using SafeMath for uint256;

    function balance() public view returns(uint256) {
        return m_balance;
    }

    function addBalance(uint256 num) public {
        m_balance = m_balance.add(num);
    }

    function subBalance(uint256 num) public {
        m_balance = m_balance.sub(num);
    }

    function transferToYourself(address to, uint256 num) public
    {
        ExecutorIntegrationTest toAccount = ExecutorIntegrationTest(address (this));
        toAccount.subBalance(num);
        toAccount.addBalance(num);
    }
    function transfer(address to, uint256 num) public
    {
        ExecutorIntegrationTest toAccount = ExecutorIntegrationTest(address (to));
        toAccount.subBalance(num);
        toAccount.addBalance(num);
    }

    uint256 m_balance;
}
