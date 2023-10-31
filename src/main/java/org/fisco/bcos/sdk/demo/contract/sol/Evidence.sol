// SPDX-License-Identifier: Apache-2.0
pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;

contract EvidenceSignersDataABI {
    function verify(address addr) public view returns (bool) {}

    function getSigner(uint index) public view returns (address) {}

    function getSignersSize() public view returns (uint) {}
}

contract Evidence {
    string evidence;
    string evidenceInfo;
    string evidenceId;
    uint8[] _v;
    bytes32[] _r;
    bytes32[] _s;
    address[] signers;
    address public signersAddr;

    event addSignaturesEvent(
        string evi,
        string info,
        string id,
        uint8 v,
        bytes32 r,
        bytes32 s
    );
    event newSignaturesEvent(
        string evi,
        string info,
        string id,
        uint8 v,
        bytes32 r,
        bytes32 s,
        address addr
    );
    event errorNewSignaturesEvent(
        string evi,
        string info,
        string id,
        uint8 v,
        bytes32 r,
        bytes32 s,
        address addr
    );
    event errorAddSignaturesEvent(
        string evi,
        string info,
        string id,
        uint8 v,
        bytes32 r,
        bytes32 s,
        address addr
    );
    event addRepeatSignaturesEvent(
        string evi,
        string info,
        string id,
        uint8 v,
        bytes32 r,
        bytes32 s
    );
    event errorRepeatSignaturesEvent(
        string evi,
        string id,
        uint8 v,
        bytes32 r,
        bytes32 s,
        address addr
    );

    function CallVerify(address addr) public view returns (bool) {
        return EvidenceSignersDataABI(signersAddr).verify(addr);
    }

    constructor(
        string memory evi,
        string memory info,
        string memory id,
        uint8 v,
        bytes32 r,
        bytes32 s,
        address addr,
        address sender
    ) {
        signersAddr = addr;
        if (CallVerify(sender)) {
            evidence = evi;
            evidenceInfo = info;
            evidenceId = id;
            _v.push(v);
            _r.push(r);
            _s.push(s);
            signers.push(sender);
            emit newSignaturesEvent(evi, info, id, v, r, s, addr);
        } else {
            emit errorNewSignaturesEvent(evi, info, id, v, r, s, addr);
        }
    }

    function getEvidenceInfo() public view returns (string memory) {
        return evidenceInfo;
    }

    function getEvidence()
        public
        view
        returns (
            string memory,
            string memory,
            string memory,
            uint8[] memory,
            bytes32[] memory,
            bytes32[] memory,
            address[] memory
        )
    {
        uint length = EvidenceSignersDataABI(signersAddr).getSignersSize();
        address[] memory signerList = new address[](length);
        for (uint i = 0; i < length; i++) {
            signerList[i] = (EvidenceSignersDataABI(signersAddr).getSigner(i));
        }
        return (evidence, evidenceInfo, evidenceId, _v, _r, _s, signerList);
    }

    function addSignatures(
        uint8 v,
        bytes32 r,
        bytes32 s
    ) public returns (bool) {
        for (uint i = 0; i < signers.length; i++) {
            if (msg.sender == signers[i]) {
                if (_v[i] == v && _r[i] == r && _s[i] == s) {
                    emit addRepeatSignaturesEvent(
                        evidence,
                        evidenceInfo,
                        evidenceId,
                        v,
                        r,
                        s
                    );
                    return true;
                } else {
                    emit errorRepeatSignaturesEvent(
                        evidence,
                        evidenceId,
                        v,
                        r,
                        s,
                        msg.sender
                    );
                    return false;
                }
            }
        }
        if (CallVerify(msg.sender)) {
            _v.push(v);
            _r.push(r);
            _s.push(s);
            signers.push(msg.sender);
            emit addSignaturesEvent(
                evidence,
                evidenceInfo,
                evidenceId,
                v,
                r,
                s
            );
            return true;
        } else {
            emit errorAddSignaturesEvent(
                evidence,
                evidenceInfo,
                evidenceId,
                v,
                r,
                s,
                msg.sender
            );
            return false;
        }
    }

    function getSigners() public view returns (address[] memory) {
        uint length = EvidenceSignersDataABI(signersAddr).getSignersSize();
        address[] memory signerList = new address[](length);
        for (uint i = 0; i < length; i++) {
            signerList[i] = (EvidenceSignersDataABI(signersAddr).getSigner(i));
        }
        return signerList;
    }
}
