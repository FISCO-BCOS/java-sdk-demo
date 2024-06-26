pragma solidity ^0.8.25;

import "./StorageSlot.sol";
import "./ContractA.sol";


contract ContractB {

    function callContractA(address a) public returns (int256){
        ContractA a = ContractA(a);
        int256 result = a.getData();
        return result;
    }

}