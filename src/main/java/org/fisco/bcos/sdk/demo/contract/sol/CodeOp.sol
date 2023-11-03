pragma solidity ^0.8.11;

contract CodeOp {
    // CODESIZE

    // CODECOPY

    // EXTCODESIZE

    // EXTCODECOPY

    // EXTCODEHASH

    function getCode(address _contractAddress) public view returns (bytes memory) {
        uint256 codeSize;
        assembly {
            codeSize := extcodesize(_contractAddress)
        }
        require(codeSize > 0, "Contract does not exist");

        bytes memory code = new bytes(codeSize);
        assembly {
            extcodecopy(_contractAddress, add(code, 32), 0, codeSize)
        }
        return code;
    }

    function getSelfCode() public view returns (bytes memory) {
        uint256 codeSize;
        assembly {
            codeSize := codesize()
        }
        require(codeSize > 0, "Contract does not exist");

        bytes memory code = new bytes(codeSize);
        assembly {
            codecopy(add(code, 32), 0, codeSize)
            mstore(code, codeSize)
        }
        return code;
    }

    function check() public returns(bytes32, bytes32, bytes32){
        bytes32 codeHash;
        address addr = address(this);
        assembly {
            codeHash := extcodehash(addr)
        }
        bytes memory code = addr.code;
        bytes memory extcode = getCode(address(this));
        bytes memory selfCode = getSelfCode();

        require(keccak256(code) == codeHash, "code must same");
        require(keccak256(extcode) == codeHash, "extcode must same");
        require(keccak256(selfCode) == codeHash, "selfCode must same");

        require(code.length == codeSize(), "codeSize must same");
        require(code.length == extcodeSize(address(this)), "extcodeSize must same");
        return (codeHash, keccak256(code), keccak256(extcode));
    }

    function codeSize() public view returns(uint256) {
        uint256 size;
        assembly {
            size := codesize()
        }
        return size;
    }

    function extcodeSize(address a) public view returns(uint256) {
        uint256 size;
        assembly {
            size := extcodesize(a)
        }
        return size;
    }
}