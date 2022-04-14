pragma solidity ^0.6.0;
pragma experimental ABIEncoderV2;

import "./SafeMath.sol";

contract Account
{
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
    
    function transfer(address to, uint256 num) public
    {
        if (to != address(this)) {
            uint256 _balance = m_balance;

            Account toAccount = Account(to); // DMC out
            toAccount.addBalance(num);

            // To check the _balance is the same after DMC scheduling out
            m_balance = _balance - num;
        } else {
            subBalance(num);
            Account toAccount = Account(to); // DMC out
            toAccount.addBalance(num);
        }
    }
    
    uint256 m_balance;
}