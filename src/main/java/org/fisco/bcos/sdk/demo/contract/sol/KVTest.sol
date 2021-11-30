// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.6.0;
pragma experimental ABIEncoderV2;

import "./KVTable.sol";

contract KVTest {
    KVTable kv_table;

    event SetEvent(int256 count);
    string constant TABLE_NAME = "person";
    constructor() public {
        kv_table = KVTable(0x1009);
        kv_table.createTable(TABLE_NAME, "name", "age,tel");
    }

    function get(string memory name)
    public
    view
    returns (
        string memory,
        string memory,
        string memory
    )
    {
        bool ok = false;
        Entry memory entry;
        (ok, entry) = kv_table.get(TABLE_NAME, name);
        string memory age;
        string memory tel;
        if (ok) {
            age = entry.fields[0].value;
            tel = entry.fields[1].value;
            return (name, age, tel);
        }
        return ("", "", "");
    }

    function set(
        string memory name,
        string memory age,
        string memory tel
    ) public returns (int256) {
        KVField memory kv1 = KVField("age", age);
        KVField memory kv2 = KVField("tel", tel);
        KVField[] memory KVFields = new KVField[](2);
        KVFields[0] = kv1;
        KVFields[1] = kv2;
        Entry memory entry = Entry(KVFields);

        // the second parameter length of set should <= 255B
        int256 count = kv_table.set(TABLE_NAME, name, entry);
        emit SetEvent(count);
        return count;
    }
}
