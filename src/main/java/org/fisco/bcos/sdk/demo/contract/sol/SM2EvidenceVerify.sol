pragma solidity >=0.6.10 <0.8.20;
pragma experimental ABIEncoderV2;
import "./Evidence.sol";
import "./Crypto.sol";

contract SM2EvidenceVerify
{
    Crypto crypto;
    event newEvidenceEvent(address addr);
	constructor() public
	{
		crypto = Crypto(address(0x100a));
	}
    function insertEvidence(string memory evi, string memory info, string memory id, address signAddr, bytes32 message, bytes memory pubKey, bytes32 r, bytes32 s) public returns(bool, address)
    {
        Evidence evidence = new Evidence(evi, info, id);
        emit newEvidenceEvent(address(evidence));
		address recoverSignAddr;
		bool succ = false;
		
		(succ, recoverSignAddr) = recoverSigner(message, pubKey, r, s);
        require( recoverSignAddr == signAddr);
		require( succ == true);
    }

   function recoverSigner(bytes32 message, bytes memory pubKey, bytes32 r, bytes32 s)
        internal
        view
        returns (bool, address)
    {
        return crypto.sm2Verify(message, pubKey, r, s);
    }
}
