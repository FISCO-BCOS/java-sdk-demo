pragma solidity ^0.6.0;

import "./ParallelContract.sol";

contract ParallelCpuHeavy is ParallelContract {
    event finish(uint size, uint signature);

    function sort(uint size, uint signature) public {
        uint[] memory data = new uint[](size);
        for (uint x = 0; x < data.length; x++) {
            data[x] = size-x;
        }
        quickSort(data, 0, int(data.length - 1));
        emit finish(size, signature);
    }

    function quickSort(uint[] memory arr, int left, int right) private {
        int i = left;
        int j = right;
        if (i == j) return;
        uint pivot = arr[uint(left + (right - left) / 2)];
        while (i <= j) {
            while (arr[uint(i)] < pivot) i++;
            while (pivot < arr[uint(j)]) j--;
            if (i <= j) {
                (arr[uint(i)], arr[uint(j)]) = (arr[uint(j)], arr[uint(i)]);
                i++;
                j--;
            }
        }
        if (left < j)
            quickSort(arr, left, j);
        if (i < right)
            quickSort(arr, i, right);
    }

    // Register parallel function
    function enableParallel() public override
    {
        // critical number is to define how many critical params from start
        registerParallelFunction("sort(uint256,uint256)", 0); // full para execution
    }

    // Disable register parallel function
    function disableParallel() public override
    {
        unregisterParallelFunction("sort(uint256,uint256)");
    }

}