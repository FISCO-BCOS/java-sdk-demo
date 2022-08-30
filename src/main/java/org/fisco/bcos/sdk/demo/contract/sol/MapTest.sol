// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.0 < 0.8.20;
pragma experimental ABIEncoderV2;

contract MapTest {
    mapping (string => string) _mapSet;

    /////////////////////////string//////////////////////
    function get(string memory key) public view returns (string memory) {
        return _mapSet[key];
    }

    function set(string memory k, string memory v) public {
        _mapSet[k] = v;
    }
}