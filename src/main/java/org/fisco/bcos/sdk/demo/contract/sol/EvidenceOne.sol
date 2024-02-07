pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;
contract EvidenceOne{

    mapping (int256 => string) evidence;
    // event newEvidenceEvent(string evi, int256 id);

    function getEvidence(int256 id) public view returns(string memory){
        return(evidence[id]);
    }
    function setEvidence(int256 id, string memory evi) public{
        evidence[id] = evi;
    }
}
