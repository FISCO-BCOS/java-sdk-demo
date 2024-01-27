import "./CodeOp.sol";
import "./Create2.sol";
import "./DelegateCall.sol";
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

    function checkOne(string memory name, address addr) private {
        try ContractTestAll(addr).check() { // just a little trick to call check() in the target contract
            // success
        } catch (bytes memory reason) {
            revert(string(abi.encodePacked(name, " check failed: ", reason)));
        }
        return; // TODO: disable this
        // use proxy to call check()
        Proxy proxy = new ProxyImpl(addr);
        (bool success, bytes memory reason) = address(proxy).call(abi.encodeWithSignature("check()"));
        require(success, string(abi.encodePacked(name, " proxy check failed: ", reason)));
    }

    function check() public {
        checkOne("CodeOp", address(new CodeOp()));
        checkOne("Create2", address(new Create2()));
        checkOne("DelegateCall", address(new DelegateCall()));
        checkOne("EventTest", address(new EventTest()));
        checkOne("LibraryTest", address(new LibraryTest()));
        checkOne("ProxyTest", address(new ProxyTest()));
        checkOne("StaticCall", address(new StaticCall()));
        checkOne("TablePrecompiledTest", address(new TablePrecompiledTest()));
        checkOne("ECRecoverTest", address(new ECRecoverTest()));
        checkOne("DeployTreeTest", address(new DeployTreeTest())); // DMC
        //checkOne("BalanceTest", address(new BalanceTest()));
        //checkOne("BalancePrecompiledTest", address(new BalancePrecompiledTest()));
        // gas price test
    }
}
