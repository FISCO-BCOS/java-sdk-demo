// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;
import "./SimpleEvidence.sol";

contract SimpleEvidenceFactory {
    event newEvidenceEvent(address addr);

    function newEvidence(
        string memory evidence,
        int256 id
    ) public {
        SimpleEvidence evi = new SimpleEvidence(evidence, id);
        emit newEvidenceEvent(address(evi));
    }
}
