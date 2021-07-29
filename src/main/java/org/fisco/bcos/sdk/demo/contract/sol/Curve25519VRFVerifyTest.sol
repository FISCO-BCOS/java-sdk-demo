pragma solidity>=0.4.24 <0.6.11;
pragma experimental ABIEncoderV2;
import "./Crypto.sol";

contract Curve25519VRFVerifyTest
{
    Crypto crypto;
	constructor() public
	{
		crypto = Crypto(0x5006);
	}
    function curve25519VRFVerify(string input, string vrfPublicKey, string vrfProof) public returns(bool, uint256)
    {
		return crypto.curve25519VRFVerify(input, vrfPublicKey, vrfProof);
    }
}
