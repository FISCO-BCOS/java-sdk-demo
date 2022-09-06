// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

import "./Table.sol";

contract MultiTableTest {


    TableManager constant tm =  TableManager(address(0x1002));
    Table table;
    string constant TABLE_NAME = "t_test";
    constructor () public{
        // create table
        string[] memory columnNames = new string[](10);
        columnNames[0] = "v0";
        columnNames[1] = "v1";
        columnNames[2] = "v2";
        columnNames[3] = "v3";
        columnNames[4] = "v4";
        columnNames[5] = "v5";
        columnNames[6] = "v6";
        columnNames[7] = "v7";
        columnNames[8] = "v8";
        columnNames[9] = "v9";
        TableInfo memory tf = TableInfo("key", columnNames);

        tm.createTable(TABLE_NAME, tf);
        address t_address = tm.openTable(TABLE_NAME);
        require(t_address!=address(0x0),"");
        table = Table(t_address);
    }

    function get(string memory id) public view returns (string memory,string memory
    ,string memory,string memory,string memory,string memory,string memory,string memory,string memory
    ,string memory)
    {
        Entry memory entry = table.select(id);
        return (entry.fields[0],entry.fields[1],entry.fields[2],entry.fields[3],entry.fields[4],entry.fields[5],entry.fields[6],entry.fields[7],entry.fields[8],entry.fields[9]);
    }

    function set(string memory id,string memory v) public{
        string[] memory columns = new string[](10);
        columns[0] = v;
        columns[1] = v;
        columns[2] = v;
        columns[3] = v;
        columns[4] = v;
        columns[5] = v;
        columns[6] = v;
        columns[7] = v;
        columns[8] = v;
        columns[9] = v;
        Entry memory entry = Entry(id, columns);
        table.insert(entry);
    }
}