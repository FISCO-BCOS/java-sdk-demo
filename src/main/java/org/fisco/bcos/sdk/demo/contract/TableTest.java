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
        "60806040523480156200001157600080fd5b5060408051600280825260608201909252600091816020015b60608152602001906001900390816200002a579050509050604051806040016040528060048152602001636e616d6560e01b8152508160008151811062000075576200007562000280565b60200260200101819052506040518060400160405280600381526020016261676560e81b81525081600181518110620000b257620000b262000280565b602090810291909101810191909152604080516080810182526002818301908152611a5960f21b606083015281528083018490528151808301835260068152651d17dd195cdd60d21b9381019390935290516318d2d28f60e11b81529091600091611002916331a5a51e916200012e91908690600401620002e6565b6020604051808303816000875af11580156200014e573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019062000174919062000383565b90508060030b600014620001ce5760405162461bcd60e51b815260206004820152601360248201527f637265617465207461626c65206661696c656400000000000000000000000000604482015260640160405180910390fd5b60408051808201825260068152651d17dd195cdd60d21b6020820152905163f23f63c960e01b81526000916110029163f23f63c9916200021191600401620003af565b602060405180830381865afa1580156200022f573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620002559190620003c4565b600080546001600160a01b0319166001600160a01b039290921691909117905550620003ef92505050565b634e487b7160e01b600052603260045260246000fd5b6000815180845260005b81811015620002be57602081850181015186830182015201620002a0565b81811115620002d1576000602083870101525b50601f01601f19169290920160200192915050565b604081526000620002fb604083018562000296565b60208382038185015284516040835262000319604084018262000296565b9050818601518382038385015281935080518083528383019450838160051b840101848301925060005b828110156200037557601f198583030187526200036282855162000296565b9686019693860193915060010162000343565b509998505050505050505050565b6000602082840312156200039657600080fd5b81518060030b8114620003a857600080fd5b9392505050565b602081526000620003a8602083018462000296565b600060208284031215620003d757600080fd5b81516001600160a01b0381168114620003a857600080fd5b610d9380620003ff6000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c80632fe99bdc1461006757806331c3e4561461009257806355f150f1146100a55780636a5bae4e146100bb57806380599e4b146100dc578063fcd7e3c1146100ef575b600080fd5b61007a610075366004610798565b610110565b60405160039190910b81526020015b60405180910390f35b61007a6100a0366004610798565b61024d565b6100ad6103bf565b60405161008992919061087c565b6100ce6100c9366004610912565b610450565b604051908152602001610089565b61007a6100ea366004610a0a565b610518565b6101026100fd366004610a0a565b6105cc565b604051610089929190610a47565b60408051600280825260608201909252600091829190816020015b606081526020019060019003908161012b579050509050838160008151811061015657610156610a75565b6020026020010181905250828160018151811061017557610175610a75565b602090810291909101810191909152604080518082018252878152918201839052600080549151635c6e105f60e01b815290916001600160a01b031690635c6e105f906101c6908590600401610b05565b6020604051808303816000875af11580156101e5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102099190610b1f565b604051600382900b81529091507fc57b01fa77f41df77eaab79a0e2623fab2e7ae3e9530d9b1cab225ad65f2b7ce9060200160405180910390a19695505050505050565b60408051600280825260608201909252600091829190816020015b6040805180820190915260008152606060208201528152602001906001900390816102685790505090506040518060400160405280600063ffffffff16815260200185815250816000815181106102c1576102c1610a75565b60200260200101819052506040518060400160405280600163ffffffff16815260200184815250816001815181106102fb576102fb610a75565b6020908102919091010152600080546040516384e3197360e01b81526001600160a01b03909116906384e31973906103399089908690600401610b42565b6020604051808303816000875af1158015610358573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061037c9190610b1f565b604051600382900b81529091507f8e5890af40fc24a059396aca2f83d6ce41fcef086876548fa4fb8ec27e9d292a9060200160405180910390a195945050505050565b60608060008060009054906101000a90046001600160a01b03166001600160a01b03166355f150f16040518163ffffffff1660e01b8152600401600060405180830381865afa158015610416573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f1916820160405261043e9190810190610cf0565b80516020909101519094909350915050565b6040805180820182528381526020810183905290516318d2d28f60e11b8152600091908290611002906331a5a51e9061048f9089908690600401610d25565b6020604051808303816000875af11580156104ae573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906104d29190610b1f565b604051600382900b81529091507fb5636cd912a73dcdb5b570dbe331dfa3e6435c93e029e642def2c8e40dacf2109060200160405180910390a160030b95945050505050565b600080546040516380599e4b60e01b815282916001600160a01b0316906380599e4b90610549908690600401610d4a565b6020604051808303816000875af1158015610568573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061058c9190610b1f565b604051600382900b81529091507f4b930e280fe29620bdff00c88155d46d6d82a39f45dd5c3ea114dc31573581129060200160405180910390a192915050565b6000805460405163fcd7e3c160e01b8152606092839290916001600160a01b039091169063fcd7e3c190610604908790600401610d4a565b600060405180830381865afa158015610621573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526106499190810190610cf0565b90506060808260200151516002141561069f57826020015160008151811061067357610673610a75565b60200260200101519150826020015160018151811061069457610694610a75565b602002602001015190505b909590945092505050565b634e487b7160e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff811182821017156106e3576106e36106aa565b60405290565b604051601f8201601f1916810167ffffffffffffffff81118282101715610712576107126106aa565b604052919050565b600067ffffffffffffffff821115610734576107346106aa565b50601f01601f191660200190565b600082601f83011261075357600080fd5b81356107666107618261071a565b6106e9565b81815284602083860101111561077b57600080fd5b816020850160208301376000918101602001919091529392505050565b6000806000606084860312156107ad57600080fd5b833567ffffffffffffffff808211156107c557600080fd5b6107d187838801610742565b945060208601359150808211156107e757600080fd5b6107f387838801610742565b9350604086013591508082111561080957600080fd5b5061081686828701610742565b9150509250925092565b60005b8381101561083b578181015183820152602001610823565b8381111561084a576000848401525b50505050565b60008151808452610868816020860160208601610820565b601f01601f19169290920160200192915050565b60408152600061088f6040830185610850565b6020838203818501528185518084528284019150828160051b85010183880160005b838110156108df57601f198784030185526108cd838351610850565b948601949250908501906001016108b1565b50909998505050505050505050565b600067ffffffffffffffff821115610908576109086106aa565b5060051b60200190565b60008060006060848603121561092757600080fd5b833567ffffffffffffffff8082111561093f57600080fd5b61094b87838801610742565b945060209150818601358181111561096257600080fd5b61096e88828901610742565b94505060408601358181111561098357600080fd5b8601601f8101881361099457600080fd5b80356109a2610761826108ee565b81815260059190911b8201840190848101908a8311156109c157600080fd5b8584015b838110156109f9578035868111156109dd5760008081fd5b6109eb8d8983890101610742565b8452509186019186016109c5565b508096505050505050509250925092565b600060208284031215610a1c57600080fd5b813567ffffffffffffffff811115610a3357600080fd5b610a3f84828501610742565b949350505050565b604081526000610a5a6040830185610850565b8281036020840152610a6c8185610850565b95945050505050565b634e487b7160e01b600052603260045260246000fd5b6000815160408452610aa06040850182610850565b9050602080840151858303828701528281518085528385019150838160051b860101848401935060005b82811015610af857601f19878303018452610ae6828651610850565b94860194938601939150600101610aca565b5098975050505050505050565b602081526000610b186020830184610a8b565b9392505050565b600060208284031215610b3157600080fd5b81518060030b8114610b1857600080fd5b60006040808352610b5581840186610850565b6020848203818601528186518084528284019150828160051b85010183890160005b83811015610bbc57868303601f190185528151805163ffffffff168452860151868401899052610ba989850182610850565b9587019593505090850190600101610b77565b50909a9950505050505050505050565b600082601f830112610bdd57600080fd5b8151610beb6107618261071a565b818152846020838601011115610c0057600080fd5b",
        "610a3f826020830160208701610820565b600060408284031215610c2357600080fd5b610c2b6106c0565b9050815167ffffffffffffffff80821115610c4557600080fd5b610c5185838601610bcc565b8352602091508184015181811115610c6857600080fd5b8401601f81018613610c7957600080fd5b8051610c87610761826108ee565b81815260059190911b82018401908481019088831115610ca657600080fd5b8584015b83811015610cde57805186811115610cc25760008081fd5b610cd08b8983890101610bcc565b845250918601918601610caa565b50808688015250505050505092915050565b600060208284031215610d0257600080fd5b815167ffffffffffffffff811115610d1957600080fd5b610a3f84828501610c11565b604081526000610d386040830185610850565b8281036020840152610a6c8185610a8b565b602081526000610b18602083018461085056fea26469706673582212207326e19b9ff63029501e33cc0044832b40e2fa670264c0c8a6030fb031cbfded64736f6c634300080b0033"
    };

    public static final String BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {
        "60806040523480156200001157600080fd5b5060408051600280825260608201909252600091816020015b60608152602001906001900390816200002a579050509050604051806040016040528060048152602001636e616d6560e01b8152508160008151811062000075576200007562000281565b60200260200101819052506040518060400160405280600381526020016261676560e81b81525081600181518110620000b257620000b262000281565b602090810291909101810191909152604080516080810182526002818301908152611a5960f21b606083015281528083018490528151808301835260068152651d17dd195cdd60d21b93810193909352905163656db23160e11b815290916000916110029163cadb6462916200012e91908690600401620002e7565b6020604051808303816000875af11580156200014e573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019062000174919062000384565b90508060030b600014620001cf57604051636381e58960e11b815260206004820152601360248201527f637265617465207461626c65206661696c656400000000000000000000000000604482015260640160405180910390fd5b60408051808201825260068152651d17dd195cdd60d21b602082015290516359a48b6560e01b8152600091611002916359a48b65916200021291600401620003b0565b602060405180830381865afa15801562000230573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190620002569190620003c5565b600080546001600160a01b0319166001600160a01b039290921691909117905550620003f092505050565b63b95aa35560e01b600052603260045260246000fd5b6000815180845260005b81811015620002bf57602081850181015186830182015201620002a1565b81811115620002d2576000602083870101525b50601f01601f19169290920160200192915050565b604081526000620002fc604083018562000297565b6020838203818501528451604083526200031a604084018262000297565b9050818601518382038385015281935080518083528383019450838160051b840101848301925060005b828110156200037657601f198583030187526200036382855162000297565b9686019693860193915060010162000344565b509998505050505050505050565b6000602082840312156200039757600080fd5b81518060030b8114620003a957600080fd5b9392505050565b602081526000620003a9602083018462000297565b600060208284031215620003d857600080fd5b81516001600160a01b0381168114620003a957600080fd5b610d9380620004006000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c80631724a182146100675780635b325d7814610092578063753582a4146100b357806386b733f9146100c95780639b6ba099146100dc578063beb9d9ed146100ef575b600080fd5b61007a610075366004610798565b610110565b60405160039190910b81526020015b60405180910390f35b6100a56100a0366004610820565b61024d565b6040516100899291906108b9565b6100bb61032b565b6040516100899291906108e7565b61007a6100d7366004610820565b6103bc565b61007a6100ea366004610798565b610470565b6101026100fd36600461097d565b6105e2565b604051908152602001610089565b60408051600280825260608201909252600091829190816020015b606081526020019060019003908161012b579050509050838160008151811061015657610156610a75565b6020026020010181905250828160018151811061017557610175610a75565b6020908102919091018101919091526040805180820182528781529182018390526000805491516304c8b95360e41b815290916001600160a01b031690634c8b9530906101c6908590600401610b05565b6020604051808303816000875af11580156101e5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906102099190610b1f565b604051600382900b81529091507fdfc533ec2b52797a1229dc2495dbd3f4948f7c4c982ec077ad9d80810ec5c1f99060200160405180910390a19695505050505050565b60008054604051630b664baf60e31b8152606092839290916001600160a01b0390911690635b325d7890610285908790600401610b42565b600060405180830381865afa1580156102a2573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526102ca9190810190610c79565b9050606080826020015151600214156103205782602001516000815181106102f4576102f4610a75565b60200260200101519150826020015160018151811061031557610315610a75565b602002602001015190505b909590945092505050565b60608060008060009054906101000a90046001600160a01b03166001600160a01b031663753582a46040518163ffffffff1660e01b8152600401600060405180830381865afa158015610382573d6000803e3d6000fd5b505050506040513d6000823e601f3d908101601f191682016040526103aa9190810190610c79565b80516020909101519094909350915050565b600080546040516386b733f960e01b815282916001600160a01b0316906386b733f9906103ed908690600401610b42565b6020604051808303816000875af115801561040c573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906104309190610b1f565b604051600382900b81529091507fe7769b56c2afa8e40381956f76b91d9ec19c34c0a81791702fdcae68e35a72719060200160405180910390a192915050565b60408051600280825260608201909252600091829190816020015b60408051808201909152600081526060602082015281526020019060019003908161048b5790505090506040518060400160405280600063ffffffff16815260200185815250816000815181106104e4576104e4610a75565b60200260200101819052506040518060400160405280600163ffffffff168152602001848152508160018151811061051e5761051e610a75565b602090810291909101015260008054604051630dbc015d60e41b81526001600160a01b039091169063dbc015d09061055c9089908690600401610cae565b6020604051808303816000875af115801561057b573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061059f9190610b1f565b604051600382900b81529091507fd72ab475a08df05fbd4f7f8cb4db1ad9dbdc26f54437fa6794acd97357779d2a9060200160405180910390a195945050505050565b60408051808201825283815260208101839052905163656db23160e11b81526000919082906110029063cadb6462906106219089908690600401610d38565b6020604051808303816000875af1158015610640573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906106649190610b1f565b604051600382900b81529091507f38411b2ef21b6826a8b9f48a1baa6b3388c3354ebdc5db749b35830ec581722d9060200160405180910390a160030b95945050505050565b63b95aa35560e01b600052604160045260246000fd5b6040805190810167ffffffffffffffff811182821017156106e3576106e36106aa565b60405290565b604051601f8201601f1916810167ffffffffffffffff81118282101715610712576107126106aa565b604052919050565b600067ffffffffffffffff821115610734576107346106aa565b50601f01601f191660200190565b600082601f83011261075357600080fd5b81356107666107618261071a565b6106e9565b81815284602083860101111561077b57600080fd5b816020850160208301376000918101602001919091529392505050565b6000806000606084860312156107ad57600080fd5b833567ffffffffffffffff808211156107c557600080fd5b6107d187838801610742565b945060208601359150808211156107e757600080fd5b6107f387838801610742565b9350604086013591508082111561080957600080fd5b5061081686828701610742565b9150509250925092565b60006020828403121561083257600080fd5b813567ffffffffffffffff81111561084957600080fd5b61085584828501610742565b949350505050565b60005b83811015610878578181015183820152602001610860565b83811115610887576000848401525b50505050565b600081518084526108a581602086016020860161085d565b601f01601f19169290920160200192915050565b6040815260006108cc604083018561088d565b82810360208401526108de818561088d565b95945050505050565b6040815260006108fa604083018561088d565b6020838203818501528185518084528284019150828160051b85010183880160005b8381101561094a57601f1987840301855261093883835161088d565b9486019492509085019060010161091c565b50909998505050505050505050565b600067ffffffffffffffff821115610973576109736106aa565b5060051b60200190565b60008060006060848603121561099257600080fd5b833567ffffffffffffffff808211156109aa57600080fd5b6109b687838801610742565b94506020915081860135818111156109cd57600080fd5b6109d988828901610742565b9450506040860135818111156109ee57600080fd5b8601601f810188136109ff57600080fd5b8035610a0d61076182610959565b81815260059190911b8201840190848101908a831115610a2c57600080fd5b8584015b83811015610a6457803586811115610a485760008081fd5b610a568d8983890101610742565b845250918601918601610a30565b508096505050505050509250925092565b63b95aa35560e01b600052603260045260246000fd5b6000815160408452610aa0604085018261088d565b9050602080840151858303828701528281518085528385019150838160051b860101848401935060005b82811015610af857601f19878303018452610ae682865161088d565b94860194938601939150600101610aca565b5098975050505050505050565b602081526000610b186020830184610a8b565b9392505050565b600060208284031215610b3157600080fd5b81518060030b8114610b1857600080fd5b602081526000610b18602083018461088d565b600082601f830112610b6657600080fd5b8151610b746107618261071a565b818152846020838601011115610b8957600080fd5b61085582602083016020870161085d565b600060408284031215610bac57600080fd5b610bb46106c0565b9050815167ffffffffffffffff80821115610bce57600080fd5b610bda85838601610b55565b8352602091508184015181811115610bf157600080fd5b8401601f81018613610c02576000",
        "80fd5b8051610c1061076182610959565b81815260059190911b82018401908481019088831115610c2f57600080fd5b8584015b83811015610c6757805186811115610c4b5760008081fd5b610c598b8983890101610b55565b845250918601918601610c33565b50808688015250505050505092915050565b600060208284031215610c8b57600080fd5b815167ffffffffffffffff811115610ca257600080fd5b61085584828501610b9a565b60006040808352610cc18184018661088d565b6020848203818601528186518084528284019150828160051b85010183890160005b83811015610d2857868303601f190185528151805163ffffffff168452860151868401899052610d158985018261088d565b9587019593505090850190600101610ce3565b50909a9950505050505050505050565b604081526000610d4b604083018561088d565b82810360208401526108de8185610a8b56fea2646970667358221220041cf70048b576f06d6597271d56ea91810dd998662aad0d1a0d2a02b9d5a15864736f6c634300080b0033"
    };

    public static final String SM_BINARY =
            org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {
        "[{\"inputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"CreateResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"InsertResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"RemoveResult\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"internalType\":\"int256\",\"name\":\"count\",\"type\":\"int256\"}],\"name\":\"UpdateResult\",\"type\":\"event\"},{\"conflictFields\":[{\"kind\":0}],\"inputs\":[{\"internalType\":\"string\",\"name\":\"tableName\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"key\",\"type\":\"string\"},{\"internalType\":\"string[]\",\"name\":\"fields\",\"type\":\"string[]\"}],\"name\":\"createTable\",\"outputs\":[{\"internalType\":\"int256\",\"name\":\"\",\"type\":\"int256\"}],\"selector\":[1784393294,3199850989],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"desc\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"string[]\",\"name\":\"\",\"type\":\"string[]\"}],\"selector\":[1441878257,1966441124],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"age\",\"type\":\"string\"}],\"name\":\"insert\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[803838940,388276610],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"}],\"name\":\"remove\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[2153356875,2260153337],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"}],\"name\":\"select\",\"outputs\":[{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"\",\"type\":\"string\"}],\"selector\":[4242006977,1530027384],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"string\",\"name\":\"id\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"name\",\"type\":\"string\"},{\"internalType\":\"string\",\"name\":\"age\",\"type\":\"string\"}],\"name\":\"update\",\"outputs\":[{\"internalType\":\"int32\",\"name\":\"\",\"type\":\"int32\"}],\"selector\":[834921558,2607521945],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"
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
                        Collections.<TypeReference<?>>emptyList());
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
                        Collections.<TypeReference<?>>emptyList());
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
                        Collections.<TypeReference<?>>emptyList());
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
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public String insert(String id, String name, String age, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForInsert(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_INSERT,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList());
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
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public String remove(String id, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForRemove(String id) {
        final Function function =
                new Function(
                        FUNC_REMOVE,
                        Arrays.<Type>asList(new Utf8String(id)),
                        Collections.<TypeReference<?>>emptyList());
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
                        Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public String update(String id, String name, String age, TransactionCallback callback) {
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForUpdate(String id, String name, String age) {
        final Function function =
                new Function(
                        FUNC_UPDATE,
                        Arrays.<Type>asList(
                                new Utf8String(id), new Utf8String(name), new Utf8String(age)),
                        Collections.<TypeReference<?>>emptyList());
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
