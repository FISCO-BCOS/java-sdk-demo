pragma solidity ^0.8.0;
import "./SafeMath.sol";
contract DmcTransfer {
    using SafeMath for uint256;
    mapping (address => address[])  m_nextContract;

    function balance() public view returns(uint256) {
        return m_balance;
    }

    function addBalance(uint256 num) public {
        m_balance = m_balance.add(num);
    }

    function subBalance(uint256 num) public {
        m_balance = m_balance.sub(num);
    }

    function takeShare(uint256 num, bool allowRevert) public
    {
        num--;
        addBalance(1);
        if(num == 0){
            return;
        }
        address from = msg.sender;
        address[] memory toArray = m_nextContract[from];
        uint shareMoney = num/toArray.length;
        //add restMoney
        uint restMoney = num - shareMoney*toArray.length;
        addBalance(restMoney);
        uint256 _balance = m_balance;
        for(uint i = 0; i < toArray.length; ++i) {
            address toAddr = toArray[i];
            DmcTransfer toAccount = DmcTransfer(toAddr);
            try toAccount.takeShare(shareMoney, allowRevert) {
                // if callMyself, update balance
                if (toAddr == address(this)){
                    _balance = m_balance;
                }
            } catch {
                if (allowRevert){
                    revert();
                } else{
                    _balance = _balance + shareMoney;
                }
            }
        }
        m_balance = _balance;
    }

    function addNextCall(address  from, address[] memory to) public{
        m_nextContract[from] = to;
    }

    uint256 m_balance;

}
