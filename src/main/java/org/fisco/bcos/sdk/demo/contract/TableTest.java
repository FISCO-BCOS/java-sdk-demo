package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.DynamicArray;
import org.fisco.bcos.sdk.v3.codec.datatypes.Event;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.Utf8String;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Int32;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple3;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class TableTest extends Contract {
    public static final String[] BINARY_ARRAY = {
        "60806040523480156200001157600080fd5b5060408051600280825260608201909252600091816020015b60608152602001906001900390816200002a579050509050604051806040016040528060048152602001636e616d6560e01b8152508160008151811062000075576200007562000258565b60200260200101819052506040518060400160405280600381526020016261676560e81b81525081600181518110620000b257620000b262000258565b602090810291909101810191909152604080516080810182526002818301908152611a5960f21b606083015281528083018490528151808301835260068152651d17dd195cdd60d21b9381019390935290516318d2d28f60e11b81529091611002916331a5a51e916200012a918590600401620002be565b6020604051808303816000875af11580156200014a573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906200017091906200035b565b5060408051808201825260068152651d17dd195cdd60d21b6020820152905163f23f63c960e01b81526000916110029163f23f63c991620001b49160040162000387565b602060405180830381865afa158015620001d2573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620001f891906200039c565b90506001600160a01b0381166200022f5760405162461bcd60e51b8152602060048201526000602482015260440160405180910390fd5b600080546001600160a01b0319166001600160a01b039290921691909117905550620003c79050565b634e487b7160e01b600052603260045260246000fd5b6000815180845260005b81811015620002965760208185018101518683018201520162000278565b81811115620002a9576000602083870101525b50601f01601f19169290920160200192915050565b604081526000620002d360408301856200026e565b602083820381850152845160408352620002f160408401826200026e565b9050818601518382038385015281935080518083528383019450838160051b840101848301925060005b828110156200034d57601f198583030187526200033a8285516200026e565b968601969386019391506001016200031b565b509998505050505050505050565b6000602082840312156200036e57600080fd5b81518060030b81146200038057600080fd5b9392505050565b6020815260006200038060208301846200026e565b600060208284031215620003af57600080fd5b81516001600160a01b03811681146200038057600080fd5b610ec580620003d76000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c80632fe99bdc1461006757806331c3e4561461009257806355f150f1146100a55780636a5bae4e146100bb57806380599e4b146100dc578063fcd7e3c1146100ef575b600080fd5b61007a610075366004610896565b610110565b60405160039190910b81526020015b60405180910390f35b61007a6100a0366004610896565b61024d565b6100ad6103e0565b60405161008992919061097a565b6100ce6100c9366004610a10565b61047d565b604051908152602001610089565b61007a6100ea366004610b08565b610616565b6101026100fd366004610b08565b6106ca565b604051610089929190610b45565b60408051600280825260608201909252600091829190816020015b606081526020019060019003908161012b579050509050838160008151811061015657610156610b73565b6020026020010181905250828160018151811061017557610175610b73565b602090810291909101810191909152604080518082018252878152918201839052600080549151635c6e105f60e01b815290916001600160a01b031690635c6e105f906101c6908590600401610c03565b6020604051808303816000875af11580156101e5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102099190610c1d565b604051600382900b81529091507fc57b01fa77f41df77eaab79a0e2623fab2e7ae3e9530d9b1cab225ad65f2b7ce9060200160405180910390a19695505050505050565b60408051600280825260608201909252600091829190816020015b6040805180820190915260608082526020820152815260200190600190039081610268575050604080516080810182526004918101918252636e616d6560e01b60608201529081526020810186905281519192509082906000906102ce576102ce610b73565b602002602001018190525060405180604001604052806040518060400160405280600381526020016261676560e81b8152508152602001848152508160018151811061031c5761031c610b73565b6020908102919091010152600080546040516341ffd75f60e01b81526001600160a01b03909116906341ffd75f9061035a9089908690600401610c40565b6020604051808303816000875af1158015610379573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061039d9190610c1d565b604051600382900b81529091507f8e5890af40fc24a059396aca2f83d6ce41fcef086876548fa4fb8ec27e9d292a9060200160405180910390a195945050505050565b60408051808201825260068152651d17dd195cdd60d21b602082015290516317435b5560e21b8152606091829160009161100291635d0d6d549161042691600401610cd5565b600060405180830381865afa158015610443573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261046b9190810190610e0c565b80516020909101519094909350915050565b6040805180820182528381526020810183905290516318d2d28f60e11b8152600091908290611002906331a5a51e906104bc9089908690600401610e41565b6020604051808303816000875af11580156104db573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906104ff9190610c1d565b60405163f23f63c960e01b81529091506000906110029063f23f63c99061052a908a90600401610cd5565b602060405180830381865afa158015610547573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061056b9190610e66565b90506001600160a01b0381166105b75760405162461bcd60e51b815260206004820152600d60248201526c656d707479206164647265737360981b604482015260640160405180910390fd5b600080546001600160a01b0319166001600160a01b038316179055604051600383900b81527fb5636cd912a73dcdb5b570dbe331dfa3e6435c93e029e642def2c8e40dacf2109060200160405180910390a15060030b95945050505050565b600080546040516380599e4b60e01b815282916001600160a01b0316906380599e4b90610647908690600401610cd5565b6020604051808303816000875af1158015610666573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061068a9190610c1d565b604051600382900b81529091507f4b930e280fe29620bdff00c88155d46d6d82a39f45dd5c3ea114dc31573581129060200160405180910390a192915050565b6000805460405163fcd7e3c160e01b8152606092839290916001600160a01b039091169063fcd7e3c190610702908790600401610cd5565b600060405180830381865afa15801561071f573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526107479190810190610e0c565b90506060808260200151516002141561079d57826020015160008151811061077157610771610b73565b60200260200101519150826020015160018151811061079257610792610b73565b602002602001015190505b909590945092505050565b634e487b7160e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff811182821017156107e1576107e16107a8565b60405290565b604051601f8201601f1916810167ffffffffffffffff81118282101715610810576108106107a8565b604052919050565b600067ffffffffffffffff821115610832576108326107a8565b50601f01601f191660200190565b600082601f83011261085157600080fd5b813561086461085f82610818565b6107e7565b81815284602083860101111561087957600080fd5b816020850160208301376000918101602001919091529392505050565b6000806000606084860312156108ab57600080fd5b833567ffffffffffffffff808211156108c357600080fd5b6108cf87838801610840565b945060208601359150808211156108e557600080fd5b6108f187838801610840565b9350604086013591508082111561090757600080fd5b5061091486828701610840565b9150509250925092565b60005b83811015610939578181015183820152602001610921565b83811115610948576000848401525b50505050565b6000815180845261096681602086016020860161091e565b601f01601f19169290920160200192915050565b60408152600061098d604083018561094e565b6020838203818501528185518084528284019150828160051b85010183880160005b838110156109dd57601f198784030185526109cb83835161094e565b948601949250908501906001016109af565b50909998505050505050505050565b600067ffffffffffffffff821115610a0657610a066107a8565b5060051b60200190565b600080600060608486031215610a2557600080fd5b833567ffffffffffffffff80821115610a3d57600080fd5b610a4987838801610840565b9450602091508186013581811115610a6057600080fd5b610a6c88828901610840565b945050604086013581811115610a8157600080fd5b8601601f81018813610a9257600080fd5b8035610aa061085f826109ec565b81815260059190911b8201840190848101908a831115610abf57600080fd5b8584015b83811015610af757803586811115610adb5760008081fd5b610ae98d8983890101610840565b845250918601918601610ac3565b508096505050505050509250925092565b600060208284031215610b1a57600080fd5b813567ffffffffffffffff811115610b3157600080fd5b610b3d84828501610840565b949350505050565b604081526000610b58604083018561094e565b8281036020840152610b6a818561094e565b95945050505050565b634e487b7160e01b600052603260045260246000fd5b6000815160408452610b9e604085018261094e565b9050602080840151858303828701528281518085528385019150838160051b860101848401935060005b82811015610bf657601f19878303018452610be482865161094e565b94860194938601939150600101610bc8565b5098975050505050505050565b602081526000610c166020830184610b89565b9392505050565b600060208284031215610c",
        "2f57600080fd5b81518060030b8114610c1657600080fd5b60006040808352610c538184018661094e565b6020848203818601528186518084528284019150828160051b85010183890160005b83811015610cc557868303601f1901855281518051898552610c998a86018261094e565b91880151858303868a0152919050610cb1818361094e565b968801969450505090850190600101610c75565b50909a9950505050505050505050565b602081526000610c16602083018461094e565b600082601f830112610cf957600080fd5b8151610d0761085f82610818565b818152846020838601011115610d1c57600080fd5b610b3d82602083016020870161091e565b600060408284031215610d3f57600080fd5b610d476107be565b9050815167ffffffffffffffff80821115610d6157600080fd5b610d6d85838601610ce8565b8352602091508184015181811115610d8457600080fd5b8401601f81018613610d9557600080fd5b8051610da361085f826109ec565b81815260059190911b82018401908481019088831115610dc257600080fd5b8584015b83811015610dfa57805186811115610dde5760008081fd5b610dec8b8983890101610ce8565b845250918601918601610dc6565b50808688015250505050505092915050565b600060208284031215610e1e57600080fd5b815167ffffffffffffffff811115610e3557600080fd5b610b3d84828501610d2d565b604081526000610e54604083018561094e565b8281036020840152610b6a8185610b89565b600060208284031215610e7857600080fd5b81516001600160a01b0381168114610c1657600080fdfea264697066735822122073775d4b2c7e0356b2a35712b47bc35b6ce24f3dfeab259f36874d4e9bde5ab764736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "60806040523480156200001157600080fd5b5060408051600280825260608201909252600091816020015b60608152602001906001900390816200002a579050509050604051806040016040528060048152602001636e616d6560e01b8152508160008151811062000075576200007562000259565b60200260200101819052506040518060400160405280600381526020016261676560e81b81525081600181518110620000b257620000b262000259565b602090810291909101810191909152604080516080810182526002818301908152611a5960f21b606083015281528083018490528151808301835260068152651d17dd195cdd60d21b93810193909352905163656db23160e11b815290916110029163cadb6462916200012a918590600401620002bf565b6020604051808303816000875af11580156200014a573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906200017091906200035c565b5060408051808201825260068152651d17dd195cdd60d21b602082015290516359a48b6560e01b8152600091611002916359a48b6591620001b49160040162000388565b602060405180830381865afa158015620001d2573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620001f891906200039d565b90506001600160a01b0381166200023057604051636381e58960e11b8152602060048201526000602482015260440160405180910390fd5b600080546001600160a01b0319166001600160a01b039290921691909117905550620003c89050565b63b95aa35560e01b600052603260045260246000fd5b6000815180845260005b81811015620002975760208185018101518683018201520162000279565b81811115620002aa576000602083870101525b50601f01601f19169290920160200192915050565b604081526000620002d460408301856200026f565b602083820381850152845160408352620002f260408401826200026f565b9050818601518382038385015281935080518083528383019450838160051b840101848301925060005b828110156200034e57601f198583030187526200033b8285516200026f565b968601969386019391506001016200031c565b509998505050505050505050565b6000602082840312156200036f57600080fd5b81518060030b81146200038157600080fd5b9392505050565b6020815260006200038160208301846200026f565b600060208284031215620003b057600080fd5b81516001600160a01b03811681146200038157600080fd5b610ec580620003d86000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c80631724a182146100675780635b325d7814610092578063753582a4146100b357806386b733f9146100c95780639b6ba099146100dc578063beb9d9ed146100ef575b600080fd5b61007a610075366004610896565b610110565b60405160039190910b81526020015b60405180910390f35b6100a56100a036600461091e565b61024d565b6040516100899291906109b7565b6100bb61032b565b6040516100899291906109e5565b61007a6100d736600461091e565b6103c8565b61007a6100ea366004610896565b61047c565b6101026100fd366004610a7b565b61060e565b604051908152602001610089565b60408051600280825260608201909252600091829190816020015b606081526020019060019003908161012b579050509050838160008151811061015657610156610b73565b6020026020010181905250828160018151811061017557610175610b73565b6020908102919091018101919091526040805180820182528781529182018390526000805491516304c8b95360e41b815290916001600160a01b031690634c8b9530906101c6908590600401610c03565b6020604051808303816000875af11580156101e5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102099190610c1d565b604051600382900b81529091507fdfc533ec2b52797a1229dc2495dbd3f4948f7c4c982ec077ad9d80810ec5c1f99060200160405180910390a19695505050505050565b60008054604051630b664baf60e31b8152606092839290916001600160a01b0390911690635b325d7890610285908790600401610c40565b600060405180830381865afa1580156102a2573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526102ca9190810190610d77565b9050606080826020015151600214156103205782602001516000815181106102f4576102f4610b73565b60200260200101519150826020015160018151811061031557610315610b73565b602002602001015190505b909590945092505050565b60408051808201825260068152651d17dd195cdd60d21b60208201529051632e21756b60e21b815260609182916000916110029163b885d5ac9161037191600401610c40565b600060405180830381865afa15801561038e573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526103b69190810190610d77565b80516020909101519094909350915050565b600080546040516386b733f960e01b815282916001600160a01b0316906386b733f9906103f9908690600401610c40565b6020604051808303816000875af1158015610418573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061043c9190610c1d565b604051600382900b81529091507fe7769b56c2afa8e40381956f76b91d9ec19c34c0a81791702fdcae68e35a72719060200160405180910390a192915050565b60408051600280825260608201909252600091829190816020015b6040805180820190915260608082526020820152815260200190600190039081610497575050604080516080810182526004918101918252636e616d6560e01b60608201529081526020810186905281519192509082906000906104fd576104fd610b73565b602002602001018190525060405180604001604052806040518060400160405280600381526020016261676560e81b8152508152602001848152508160018151811061054b5761054b610b73565b602090810291909101015260008054604051627ea01360e21b81526001600160a01b03909116906301fa804c906105889089908690600401610dac565b6020604051808303816000875af11580156105a7573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906105cb9190610c1d565b604051600382900b81529091507fd72ab475a08df05fbd4f7f8cb4db1ad9dbdc26f54437fa6794acd97357779d2a9060200160405180910390a195945050505050565b60408051808201825283815260208101839052905163656db23160e11b81526000919082906110029063cadb64629061064d9089908690600401610e41565b6020604051808303816000875af115801561066c573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906106909190610c1d565b6040516359a48b6560e01b8152909150600090611002906359a48b65906106bb908a90600401610c40565b602060405180830381865afa1580156106d8573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906106fc9190610e66565b90506001600160a01b03811661074957604051636381e58960e11b815260206004820152600d60248201526c656d707479206164647265737360981b604482015260640160405180910390fd5b600080546001600160a01b0319166001600160a01b038316179055604051600383900b81527f38411b2ef21b6826a8b9f48a1baa6b3388c3354ebdc5db749b35830ec581722d9060200160405180910390a15060030b95945050505050565b63b95aa35560e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff811182821017156107e1576107e16107a8565b60405290565b604051601f8201601f1916810167ffffffffffffffff81118282101715610810576108106107a8565b604052919050565b600067ffffffffffffffff821115610832576108326107a8565b50601f01601f191660200190565b600082601f83011261085157600080fd5b813561086461085f82610818565b6107e7565b81815284602083860101111561087957600080fd5b816020850160208301376000918101602001919091529392505050565b6000806000606084860312156108ab57600080fd5b833567ffffffffffffffff808211156108c357600080fd5b6108cf87838801610840565b945060208601359150808211156108e557600080fd5b6108f187838801610840565b9350604086013591508082111561090757600080fd5b5061091486828701610840565b9150509250925092565b60006020828403121561093057600080fd5b813567ffffffffffffffff81111561094757600080fd5b61095384828501610840565b949350505050565b60005b8381101561097657818101518382015260200161095e565b83811115610985576000848401525b50505050565b600081518084526109a381602086016020860161095b565b601f01601f19169290920160200192915050565b6040815260006109ca604083018561098b565b82810360208401526109dc818561098b565b95945050505050565b6040815260006109f8604083018561098b565b6020838203818501528185518084528284019150828160051b85010183880160005b83811015610a4857601f19878403018552610a3683835161098b565b94860194925090850190600101610a1a565b50909998505050505050505050565b600067ffffffffffffffff821115610a7157610a716107a8565b5060051b60200190565b600080600060608486031215610a9057600080fd5b833567ffffffffffffffff80821115610aa857600080fd5b610ab487838801610840565b9450602091508186013581811115610acb57600080fd5b610ad788828901610840565b945050604086013581811115610aec57600080fd5b8601601f81018813610afd57600080fd5b8035610b0b61085f82610a57565b81815260059190911b8201840190848101908a831115610b2a57600080fd5b8584015b83811015610b6257803586811115610b465760008081fd5b610b548d8983890101610840565b845250918601918601610b2e565b508096505050505050509250925092565b63b95aa35560e01b600052603260045260246000fd5b6000815160408452610b9e604085018261098b565b9050602080840151858303828701528281518085528385019150838160051b860101848401935060005b82811015610bf657601f19878303018452610be482865161098b565b94860194938601939150600101610bc8565b5098975050505050505050565b602081526000610c166020830184610b89565b9392505050565b60006020828403121561",
        "0c2f57600080fd5b81518060030b8114610c1657600080fd5b602081526000610c16602083018461098b565b600082601f830112610c6457600080fd5b8151610c7261085f82610818565b818152846020838601011115610c8757600080fd5b61095382602083016020870161095b565b600060408284031215610caa57600080fd5b610cb26107be565b9050815167ffffffffffffffff80821115610ccc57600080fd5b610cd885838601610c53565b8352602091508184015181811115610cef57600080fd5b8401601f81018613610d0057600080fd5b8051610d0e61085f82610a57565b81815260059190911b82018401908481019088831115610d2d57600080fd5b8584015b83811015610d6557805186811115610d495760008081fd5b610d578b8983890101610c53565b845250918601918601610d31565b50808688015250505050505092915050565b600060208284031215610d8957600080fd5b815167ffffffffffffffff811115610da057600080fd5b61095384828501610c98565b60006040808352610dbf8184018661098b565b6020848203818601528186518084528284019150828160051b85010183890160005b83811015610e3157868303601f1901855281518051898552610e058a86018261098b565b91880151858303868a0152919050610e1d818361098b565b968801969450505090850190600101610de1565b50909a9950505050505050505050565b604081526000610e54604083018561098b565b82810360208401526109dc8185610b89565b600060208284031215610e7857600080fd5b81516001600160a01b0381168114610c1657600080fdfea26469706673582212209528db0908401caa8c8bc450f22489e564a5e24bf8199ff3cf622fb5e956aeda64736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"CreateResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"InsertResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"RemoveResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"UpdateResult\",\"type\":\"event\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"tableName\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"key\",\"type\":\"string\"},{\"internalType\":\"string[]\",\"name\":\"fields\",\"type\":\"string[]\"}],\"name\":\"createTable\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"selector\":[1784393294,3199850989],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[],\"name\":\"desc\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"string[]\",\"name\":\"\",\"type\":\"string[]\"}],\"selector\":[1441878257,1966441124],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"age\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[803838940,388276610],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[2153356875,2260153337],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"}],\"name\":\"select\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"selector\":[4242006977,1530027384],\"stateMutability\":\"view\",\"type\":\"function\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"age\",\"type\":\"string\"}],\"name\":\"update\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[834921558,2607521945],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
    };

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_CREATETABLE = "createTable";

    public static final String FUNC_DESC = "desc";

    public static final String FUNC_INSERT = "insert";

    public static final String FUNC_REMOVE = "remove";

    public static final String FUNC_SELECT = "select";

    public static final String FUNC_UPDATE = "update";

    public static final Event CREATERESULT_EVENT =
            new Event(
                    "CreateResult",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    public static final Event INSERTRESULT_EVENT =
            new Event(
                    "InsertResult",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    public static final Event REMOVERESULT_EVENT =
            new Event(
                    "RemoveResult",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    public static final Event UPDATERESULT_EVENT =
            new Event(
                    "UpdateResult",
                    Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));;

    protected TableTest(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public List<CreateResultEventResponse> getCreateResultEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(CREATERESULT_EVENT, transactionReceipt);
        ArrayList<CreateResultEventResponse> responses =
                new ArrayList<CreateResultEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            CreateResultEventResponse typedResponse = new CreateResultEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<InsertResultEventResponse> getInsertResultEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(INSERTRESULT_EVENT, transactionReceipt);
        ArrayList<InsertResultEventResponse> responses =
                new ArrayList<InsertResultEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            InsertResultEventResponse typedResponse = new InsertResultEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<RemoveResultEventResponse> getRemoveResultEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(REMOVERESULT_EVENT, transactionReceipt);
        ArrayList<RemoveResultEventResponse> responses =
                new ArrayList<RemoveResultEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            RemoveResultEventResponse typedResponse = new RemoveResultEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public List<UpdateResultEventResponse> getUpdateResultEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList =
                extractEventParametersWithLog(UPDATERESULT_EVENT, transactionReceipt);
        ArrayList<UpdateResultEventResponse> responses =
                new ArrayList<UpdateResultEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            UpdateResultEventResponse typedResponse = new UpdateResultEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.count = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public TransactionReceipt createTable(String tableName, String key, List<String> fields) {
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(
                                new Utf8String(tableName),
                                new Utf8String(key),
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                fields, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String createTable(
            String tableName, String key, List<String> fields, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(
                                new Utf8String(tableName),
                                new Utf8String(key),
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                fields, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForCreateTable(
            String tableName, String key, List<String> fields) {
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(
                                new Utf8String(tableName),
                                new Utf8String(key),
                                new DynamicArray<Utf8String>(
                                        Utf8String.class,
                                        org.fisco.bcos.sdk.v3.codec.Utils.typeMap(
                                                fields, Utf8String.class))),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, List<String>> getCreateTableInput(
            TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, List<String>>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                convertToNative((List<Utf8String>) results.get(2).getValue()));
    }

    public Tuple1<BigInteger> getCreateTableOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_CREATETABLE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int256>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public Tuple2<String, List<String>> desc() throws ContractException {
        final Function function =
                new Function(
                        FUNC_DESC,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<DynamicArray<Utf8String>>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<String, List<String>>(
                (String) results.get(0).getValue(),
                convertToNative((List<Utf8String>) results.get(1).getValue()));
    }

    public TransactionReceipt insert(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String insert(String id, String name, String age, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForInsert(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, String> getInsertInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, String>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue());
    }

    public Tuple1<BigInteger> getInsertOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int32>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public TransactionReceipt remove(String id) {
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String remove(String id, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForRemove(String id) {
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple1<String> getRemoveInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>((String) results.get(0).getValue());
    }

    public Tuple1<BigInteger> getRemoveOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int32>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public Tuple2<String, String> select(String id) throws ContractException {
        final Function function =
                new Function(
                        FUNC_SELECT,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<String, String>(
                (String) results.get(0).getValue(), (String) results.get(1).getValue());
    }

    public TransactionReceipt update(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return executeTransaction(function);
    }

    public String update(String id, String name, String age, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUpdate(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList(),
                        0);
        return createSignedTransaction(function);
    }

    public Tuple3<String, String, String> getUpdateInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {},
                                new TypeReference<Utf8String>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple3<String, String, String>(
                (String) results.get(0).getValue(),
                (String) results.get(1).getValue(),
                (String) results.get(2).getValue());
    }

    public Tuple1<BigInteger> getUpdateOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Int32>() {}));
        List<Type> results =
                this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>((BigInteger) results.get(0).getValue());
    }

    public static TableTest load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new TableTest(contractAddress, client, credential);
    }

    public static TableTest deploy(Client client, CryptoKeyPair credential)
            throws ContractException {
        return deploy(
                TableTest.class,
                client,
                credential,
                getBinary(client.getCryptoSuite()),
                getABI(),
                null,
                null);
    }

    public static class CreateResultEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }

    public static class InsertResultEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }

    public static class RemoveResultEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }

    public static class UpdateResultEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger count;
    }
}
