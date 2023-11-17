
contract EventTest {
    event Echo(uint);

    function echoDFS() public {
        this.dfs(1);
        // Event: {"Echo":[[4],[5],[2],[6],[7],[3],[1]]}
    }

    function dfs(uint i) public  {
        if (i >= 8) {
            return;
        }

        this.dfs(i * 2);
        this.dfs(i * 2 + 1);
        emit Echo(i);
    }
}