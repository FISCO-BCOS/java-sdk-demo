// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

contract SmallBank {

    //uint constant MAX_ACCOUNT = 10000;
    //uint constant BALANCE = 10000;
    //bytes20 constant accountTab = "account";
    //bytes20 constant savingTab = "saving";
    //bytes20 constant checkingTab = "checking";

    mapping(string=>uint) savingStore;
    mapping(string=>uint) checkingStore;

    function almagate(string memory arg0, string memory arg1) public {
       uint bal1 = savingStore[arg0];
       uint bal2 = checkingStore[arg1];

       checkingStore[arg0] = 0;
       savingStore[arg1] = bal1 + bal2;
    }

    function getBalance(string memory arg0) public view returns (uint balance) {
        uint bal1 = savingStore[arg0];
        uint bal2 = checkingStore[arg0];

        balance = bal1 + bal2;
        return balance;
    }

    function updateBalance(string memory arg0, uint arg1) public {
        uint bal1 = checkingStore[arg0];
        uint bal2 = arg1;

        checkingStore[arg0] = bal1 + bal2;
    }

    function updateSaving(string memory arg0, uint arg1) public {
        uint bal1 = savingStore[arg0];
        uint bal2 = arg1;

        savingStore[arg0] = bal1 + bal2;
    }

    function sendPayment(string memory arg0, string memory arg1, uint arg2) public {
        checkingStore[arg0] -= arg2;
        checkingStore[arg1] += arg2;
    }

    function writeCheck(string memory arg0, uint arg1) public {
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
}
