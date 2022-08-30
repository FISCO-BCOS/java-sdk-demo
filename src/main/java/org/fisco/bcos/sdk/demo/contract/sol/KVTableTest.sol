// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract KVTableTest {

    TableManager tm;
    KVTable table;
    string constant tableName = "t_kv_test";
    constructor () public{
        tm = TableManager(address(0x1002));

        // create table
        tm.createKVTable(tableName, "key", "value");

        // get table address
        address t_address = tm.openTable(tableName);
        table = KVTable(t_address);
    }

    function get(string memory id) public view returns (bool, string memory) {
        bool ok = false;
        string memory value;
        (ok, value) = table.get(id);
        return (ok, value);
    }

    function set(string memory id, string memory item_name)
    public
    returns (int32)
    {
        int32 result = table.set(id,item_name);
        return result;
    }
}