pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;
import "./Evidence.sol";
import "./Crypto.sol";

contract SM2EvidenceVerify
{
    Crypto crypto;
    event newEvidenceEvent(address addr);
	constructor() public
	{
		crypto = Crypto(0x5006);
	}
    function insertEvidence(string evi, string info, string id, address signAddr, bytes32 message, bytes pubKey, bytes32 r, bytes32 s) public returns(bool, address)
    {
        address evidence = new Evidence(evi, info, id);
        newEvidenceEvent(evidence);
		address recoverSignAddr;
		bool succ = false;
		
		(succ, recoverSignAddr) = recoverSigner(message, pubKey, r, s);
        require( recoverSignAddr == signAddr);
		require( succ == true);
    }

   function recoverSigner(bytes32 message, bytes pubKey, bytes32 r, bytes32 s)
        internal
        view
        returns (bool, address)
    {
        return crypto.sm2Verify(message, pubKey, r, s);
    }
}
