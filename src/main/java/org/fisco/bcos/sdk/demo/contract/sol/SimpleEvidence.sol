pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;
contract SimpleEvidence{
    string evidence;
    string evidenceInfo;
    string evidenceId;
    event newEvidenceEvent(string evi, string info, string id);
    constructor(string memory evi, string memory info, string memory id) public
    {
        evidence = evi;
        evidenceInfo = info;
        evidenceId = id;
        emit newEvidenceEvent(evi,info,id);
    }

    function getEvidenceInfo() public view returns(string memory)
    {
        return evidenceInfo;
    }

    function getEvidence() public view returns(string memory,string memory,string memory){
        return(evidence,evidenceInfo,evidenceId);
    }
}
