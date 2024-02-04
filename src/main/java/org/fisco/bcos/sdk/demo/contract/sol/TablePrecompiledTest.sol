// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

import "./TableTestV320.sol";

contract TablePrecompiledTest {

    function check() public {
        TableTestV320 test = new TableTestV320();
        test.select(1, 100); // should not revert
    }


}