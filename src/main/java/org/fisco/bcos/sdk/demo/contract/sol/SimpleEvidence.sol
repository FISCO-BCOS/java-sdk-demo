pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;
contract SimpleEvidence{
    string evidence;
    int256 evidenceId;
    // event newEvidenceEvent(string evi, int256 id);
    constructor(string memory evi, int256 id) public
    {
        evidence = evi;
        evidenceId = id;
        // emit newEvidenceEvent(evi,id);
    }

    function getEvidence() public view returns(string memory,int256){
        return(evidence,evidenceId);
    }
}
