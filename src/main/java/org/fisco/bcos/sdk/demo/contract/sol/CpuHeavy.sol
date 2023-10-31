// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

contract CpuHeavy {
    event finish(uint size, uint signature);

    function sort(uint size, uint signature) public {
        uint[] memory data = new uint[](size);
        for (uint x = 0; x < data.length; x++) {
            data[x] = size-x;
        }
        quickSort(data, 0, int(data.length - 1));
        emit finish(size, signature);
    }

    function quickSort(uint[] memory arr, int left, int right) internal {
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
}

