import "./CodeOp.sol";
import "./Create2.sol";
import "./DelegateCall.sol";
import "./RecursiveDelegateCallTest.sol";
import "./EventTest.sol";
import "./LibraryTest.sol";
import "./ProxyTest.sol";
import "./StaticCall.sol";
import "./TablePrecompiledTest.sol";
import "./ECRecoverTest.sol";
import "./DeployTreeTest.sol"; // DMC
import "./BalanceTest.sol";
import "./BalancePrecompiledTest.sol";

contract ContractTestAll {

    function checkOne(string memory name, address addr, bool needProxyCheck, uint256 callValue) private {

        try ContractTestAll(addr).check{value: callValue}() { // just a little trick to call check() in the target contract
            // success
        } catch (bytes memory reason) {
            revert(string(abi.encodePacked(name, " check failed: ", reason)));
        }

        if (!needProxyCheck) {
            return;
        }

        // use proxy to call check()
        Proxy proxy = new ProxyImpl(addr);
        (bool success, bytes memory reason) = address(proxy).call{value: callValue}(abi.encodeWithSignature("check()"));
        require(success, string(abi.encodePacked(name, " proxy check failed: ", reason)));
    }

    function check() public payable {
        uint256 thisBalance = address(this).balance;
        uint256 callValue = thisBalance > 100000 ? 5000 : 0;

        checkOne("CodeOp", address(new CodeOp()), false, 0);
        checkOne("Create2", address(new Create2()), true, 0);
        checkOne("DelegateCall", address(new DelegateCall()), true, 0);
        checkOne("RecursiveDelegateCallTest", address(new RecursiveDelegateCallTest()), false, 0);
        checkOne("EventTest", address(new EventTest()), true, 0);
        checkOne("LibraryTest", address(new LibraryTest()), true, 0);
        checkOne("ProxyTest", address(new ProxyTest()), true, 0);
        checkOne("StaticCall", address(new StaticCall()), true, 0);
        checkOne("TablePrecompiledTest", address(new TablePrecompiledTest()), true, 0);
        checkOne("ECRecoverTest", address(new ECRecoverTest()), true, 0);
        checkOne("DeployTreeTest", address(new DeployTreeTest()), true, 0); // DMC
        checkOne("BalanceTest", address(new BalanceTest()), true, callValue);
        checkOne("BalancePrecompiledTest", address(new BalancePrecompiledTest()), false, 0);
    }
}
