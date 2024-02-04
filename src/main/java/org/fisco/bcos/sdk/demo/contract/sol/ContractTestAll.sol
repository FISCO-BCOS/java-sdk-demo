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
//import "./BalanceTest.sol";
import "./BalancePrecompiledTest.sol";

contract ContractTestAll {

    function checkOne(string memory name, address addr, bool needProxyCheck) private {
        try ContractTestAll(addr).check() { // just a little trick to call check() in the target contract
            // success
        } catch (bytes memory reason) {
            revert(string(abi.encodePacked(name, " check failed: ", reason)));
        }

        if (!needProxyCheck) {
            return;
        }

        // use proxy to call check()
        Proxy proxy = new ProxyImpl(addr);
        (bool success, bytes memory reason) = address(proxy).call(abi.encodeWithSignature("check()"));
        require(success, string(abi.encodePacked(name, " proxy check failed: ", reason)));
    }

    function check() public payable {

        checkOne("CodeOp", address(new CodeOp()), false);
        checkOne("Create2", address(new Create2()), true);
        checkOne("DelegateCall", address(new DelegateCall()), true);
        checkOne("RecursiveDelegateCallTest", address(new RecursiveDelegateCallTest()), false);
        checkOne("EventTest", address(new EventTest()), true);
        checkOne("LibraryTest", address(new LibraryTest()), true);
        checkOne("ProxyTest", address(new ProxyTest()), true);
        checkOne("StaticCall", address(new StaticCall()), true);
        checkOne("TablePrecompiledTest", address(new TablePrecompiledTest()), true);
        checkOne("ECRecoverTest", address(new ECRecoverTest()), true);
        checkOne("DeployTreeTest", address(new DeployTreeTest()), true); // DMC
        //checkOne("BalanceTest", address(new BalanceTest()), true);
        checkOne("BalancePrecompiledTest", address(new BalancePrecompiledTest()), false);
        // gas price test
    }
}
