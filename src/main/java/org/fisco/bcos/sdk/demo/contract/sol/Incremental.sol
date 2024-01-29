// SPDX-License-Identifier: UNLICENSED
pragma solidity>=0.6.10 <0.8.20;

contract Incremental {
    uint public value = 0;

    event incEvent(string msg);
    function inc(string memory msg) public returns(uint) {
        value += 1;
        emit incEvent(msg);
        return value;
    }
}
