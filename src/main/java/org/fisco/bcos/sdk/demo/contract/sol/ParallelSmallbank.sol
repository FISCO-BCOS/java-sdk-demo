pragma solidity ^0.6.0;
import "./ParallelContract.sol";

contract ParallelSmallbank is ParallelContract{

    mapping(string=>uint) savingStore;
    mapping(string=>uint) checkingStore;

    function almagate(string arg0, string arg1) public {
        uint bal1 = savingStore[arg0];
        uint bal2 = checkingStore[arg1];

        checkingStore[arg0] = 0;
        savingStore[arg1] = bal1 + bal2;
    }

    function getBalance(string arg0) public constant returns (uint balance) {
        uint bal1 = savingStore[arg0];
        uint bal2 = checkingStore[arg0];

        balance = bal1 + bal2;
        return balance;
    }

    function updateBalance(string arg0, uint arg1) public {
        uint bal1 = checkingStore[arg0];
        uint bal2 = arg1;

        checkingStore[arg0] = bal1 + bal2;
    }

    function updateSaving(string arg0, uint arg1) public {
        uint bal1 = savingStore[arg0];
        uint bal2 = arg1;

        savingStore[arg0] = bal1 + bal2;
    }

    function sendPayment(string arg0, string arg1, uint arg2) public {
        uint bal1 = checkingStore[arg0];
        uint bal2 = checkingStore[arg1];
        uint amount = arg2;

        bal1 -= amount;
        bal2 += amount;

        checkingStore[arg0] = bal1;
        checkingStore[arg1] = bal2;
    }

    function writeCheck(string arg0, uint arg1) public {
        uint bal1 = checkingStore[arg0];
        uint bal2 = savingStore[arg0];
        uint amount = arg1;

        if (amount < bal1 + bal2) {
            checkingStore[arg0] = bal1 - amount - 1;
        }
        else {
            checkingStore[arg0] = bal1 - amount;
        }
    }

    // Register parallel function
    function enableParallel() public override
    {
        // critical number is to define how many critical params from start
        registerParallelFunction("updatebalance(string,uint256)", 1); // critical: string
        registerParallelFunction("sendpayment(string,string,uint256)", 2); // critical: string string
        
    }

    // Disable register parallel function
    function disableParallel() public override
    {
        unregisterParallelFunction("updatebalance(string,uint256)");
        unregisterParallelFunction("sendpayment(string,string,uint256)"); 
    }
}
