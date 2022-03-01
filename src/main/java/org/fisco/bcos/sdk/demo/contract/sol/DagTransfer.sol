pragma solidity ^0.6.0;

abstract contract DagTransfer{
    function userAdd(string calldata user, uint256 balance) public virtual returns(uint256);
    function userSave(string calldata user, uint256 balance) public virtual returns(uint256);
    function userDraw(string calldata user, uint256 balance) public virtual returns(uint256);
    function userBalance(string calldata user) public virtual view returns(uint256,uint256);
    function userTransfer(string calldata user_a, string calldata user_b, uint256 amount) public virtual returns(uint256);
}
