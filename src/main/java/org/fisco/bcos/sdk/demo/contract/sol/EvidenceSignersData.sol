// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;
import "./Evidence.sol";

contract EvidenceSignersData {
    address[] signers;
    event newEvidenceEvent(address addr);

    constructor(address[] memory evidenceSigners) {
        for (uint i = 0; i < evidenceSigners.length; ++i) {
            signers.push(evidenceSigners[i]);
        }
    }

    function newEvidence(
        string memory evi,
        string memory info,
        string memory id,
        uint8 v,
        bytes32 r,
        bytes32 s
    ) public returns (address) {
        Evidence evidence = new Evidence(
            evi,
            info,
            id,
            v,
            r,
            s,
            address(this),
            msg.sender
        );
        emit newEvidenceEvent(address(evidence));
        return address(evidence);
    }

    function verify(address addr) public view returns (bool) {
        for (uint i = 0; i < signers.length; ++i) {
            if (addr == signers[i]) {
                return true;
            }
        }
        return false;
    }

    function getSigner(uint index) public view returns (address) {
        uint listSize = signers.length;
        if (index < listSize) {
            return signers[index];
        } else {
            return address(0);
        }
    }

    function getSignersSize() public view returns (uint) {
        return signers.length;
    }

    function getSigners() public view returns (address[] memory) {
        return signers;
    }
}
