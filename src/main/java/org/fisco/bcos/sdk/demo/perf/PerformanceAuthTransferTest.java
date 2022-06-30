package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.Account;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodecException;
import org.fisco.bcos.sdk.v3.contract.auth.manager.AuthManager;
import org.fisco.bcos.sdk.v3.contract.auth.po.AuthType;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.manager.AssembleTransactionProcessor;
import org.fisco.bcos.sdk.v3.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class PerformanceAuthTransferTest {
    private static final int DEFAULT_LIMIT = 100;
    private static final int DEFAULT_ADDRESS_COUNT = 10;
    private static Client client;
    private static final Random random = new Random();
    private static final List<CryptoKeyPair> addressList = new ArrayList<>();
    private static final Map<Integer, AtomicLong> accountLedger = new ConcurrentHashMap<>();
    private static final Map<String, List<CryptoKeyPair>> contractAclMap =
            new ConcurrentHashMap<>();

    public static void Usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceAuthTransferTest test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceAuthTransferTest [groupId] [userCount] [contractCount] [txCount] [qps].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl =
                    PerformanceAuthTransferTest.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                throw new IOException("The configFile " + configFileName + " doesn't exist!");
            }

            if (args.length < 5) {
                Usage();
                return;
            }
            String groupId = args[0];
            int userCount = Integer.parseInt(args[1]);
            int contractCount = Integer.parseInt(args[2]);
            int txCount = Integer.parseInt(args[3]);
            int qps = Integer.parseInt(args[4]);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            random.setSeed(System.currentTimeMillis());
            client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "PerformanceAuthTransferTest",
                            Runtime.getRuntime().availableProcessors());
            // new accounts, write to a list
            System.out.println("Start build account address, address size: " + userCount);
            buildAccount(userCount);
            System.out.println(
                    "Build account address finished, address size: " + addressList.size());

            // new contracts, write to a list
            List<Account> contracts = deployContracts(contractCount, threadPoolService);

            if (client.isAuthCheck()) {
                // set acl type to all new contract, random to white/black_list
                setContractAcl(contracts, threadPoolService);
            }
            // setMethod acl to all new contract,
            // random to accounts, random to open/close, write it to a map
            setContractMethodAcl(contracts, threadPoolService);
            // start send transaction
            start(contracts, txCount, qps, threadPoolService);

            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void buildAccount(int userCount) {
        CryptoKeyPair keyPairFactory = client.getCryptoSuite().getKeyPairFactory();
        for (int i = 0; i < userCount; i++) {
            addressList.add(keyPairFactory.generateKeyPair());
        }
    }

    private static List<Account> deployContracts(
            int contractCount, ThreadPoolService threadPoolService) throws InterruptedException {
        System.out.println("====== deployContracts, contractCount: " + contractCount);

        Account[] accounts = new Account[contractCount];
        String abi = Account.getABI();
        String binary = Account.getBinary(client.getCryptoSuite());
        AssembleTransactionProcessor assembleTransactionProcessor =
                TransactionProcessorFactory.createAssembleTransactionProcessor(
                        client, client.getCryptoSuite().getCryptoKeyPair(), "Account", abi, binary);
        RateLimiter limiter = RateLimiter.create(DEFAULT_LIMIT);

        System.out.println("Deploy contracts...");
        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(contractCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(contractCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch countDownLatch = new CountDownLatch(contractCount);
        for (int i = 0; i < contractCount; ++i) {
            final int index = i;
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                try {
                                    assembleTransactionProcessor.deployAsync(
                                            abi,
                                            binary,
                                            new ArrayList<>(),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long initBalance = Math.abs(random.nextLong());
                                                    if (receipt.getStatus() != 0) {
                                                        countDownLatch.countDown();
                                                        receivedBar.step();
                                                        return;
                                                    }
                                                    contractAclMap.put(
                                                            receipt.getContractAddress(),
                                                            new ArrayList<>());
                                                    Account account =
                                                            Account.load(
                                                                    receipt.getContractAddress(),
                                                                    client,
                                                                    client.getCryptoSuite()
                                                                            .getCryptoKeyPair());
                                                    accounts[index] = account;
                                                    account.addBalance(
                                                            BigInteger.valueOf(initBalance),
                                                            new TransactionCallback() {
                                                                @Override
                                                                public void onResponse(
                                                                        TransactionReceipt
                                                                                receipt) {
                                                                    if (receipt.getStatus() != 0) {
                                                                        countDownLatch.countDown();
                                                                        receivedBar.step();
                                                                        return;
                                                                    }
                                                                    accountLedger.put(
                                                                            index,
                                                                            new AtomicLong(
                                                                                    initBalance));
                                                                    countDownLatch.countDown();
                                                                    receivedBar.step();
                                                                }
                                                            });
                                                }
                                            });
                                    sentBar.step();
                                } catch (ContractCodecException e) {
                                    e.printStackTrace();
                                }
                            });
        }
        countDownLatch.await();
        System.out.println("Create account finished!");
        sentBar.close();
        receivedBar.close();

        return Arrays.asList(accounts);
    }

    private static void setContractAcl(List<Account> accounts, ThreadPoolService threadPoolService)
            throws InterruptedException {
        System.out.println("====== setContractACL, contractCount: " + accounts.size());
        AuthManager authManager =
                new AuthManager(client, client.getCryptoSuite().getCryptoKeyPair());
        byte[] hash = client.getCryptoSuite().hash("addBalance(uint256)".getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        RateLimiter limiter = RateLimiter.create(DEFAULT_LIMIT);

        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(accounts.size())
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(accounts.size())
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch countDownLatch = new CountDownLatch(accounts.size());

        for (Account account : accounts) {
            limiter.acquire();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                authManager.asyncSetMethodAuthType(
                                        account.getContractAddress(),
                                        func,
                                        random.nextBoolean()
                                                ? AuthType.BLACK_LIST
                                                : AuthType.WHITE_LIST,
                                        retCode -> {
                                            receivedBar.step();
                                            countDownLatch.countDown();
                                        });
                                sentBar.step();
                            });
        }
        countDownLatch.await();
        sentBar.close();
        receivedBar.close();
        System.out.println("Set contract acl finished!");
    }

    private static void setContractMethodAcl(
            List<Account> accounts, ThreadPoolService threadPoolService)
            throws InterruptedException {
        System.out.println("====== setContractMethodAcl, contracts size: " + accounts.size());

        AuthManager authManager =
                new AuthManager(client, client.getCryptoSuite().getCryptoKeyPair());
        byte[] hash = client.getCryptoSuite().hash("addBalance(uint256)".getBytes());
        byte[] func = Arrays.copyOfRange(hash, 0, 4);
        RateLimiter limiter = RateLimiter.create(DEFAULT_LIMIT);

        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax((long) accounts.size() * DEFAULT_ADDRESS_COUNT)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax((long) accounts.size() * DEFAULT_ADDRESS_COUNT)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch countDownLatch = new CountDownLatch(accounts.size() * DEFAULT_ADDRESS_COUNT);

        List<CryptoKeyPair> authAddressList = new ArrayList<>(DEFAULT_ADDRESS_COUNT);
        for (int i = 0; i < DEFAULT_ADDRESS_COUNT; i++) {
            authAddressList.add(addressList.get(random.nextInt(addressList.size())));
        }

        for (Account account : accounts) {
            for (CryptoKeyPair address : authAddressList) {
                limiter.acquire();
                if (client.isAuthCheck()) {
                    threadPoolService
                            .getThreadPool()
                            .execute(
                                    () -> {
                                        boolean isOpen = random.nextBoolean();
                                        authManager.asyncSetMethodAuth(
                                                account.getContractAddress(),
                                                func,
                                                address.getAddress(),
                                                isOpen,
                                                retCode -> {
                                                    contractAclMap
                                                            .get(account.getContractAddress())
                                                            .add(address);
                                                    receivedBar.step();
                                                    countDownLatch.countDown();
                                                });
                                        sentBar.step();
                                    });
                } else {
                    sentBar.step();
                    contractAclMap.get(account.getContractAddress()).add(address);
                    receivedBar.step();
                    countDownLatch.countDown();
                }
            }
        }
        countDownLatch.await();
        sentBar.close();
        receivedBar.close();
        System.out.println("Set contract acl method finished!");
    }

    public static void start(
            List<Account> contracts, int count, int qps, ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {

        RateLimiter limiter = RateLimiter.create(qps);

        int sendContractCount = count / DEFAULT_ADDRESS_COUNT;

        System.out.println("Sending transactions...");
        ProgressBar sentBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        System.out.println(
                "====== Start send transaction test, tx count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", sendContractCount: "
                        + sendContractCount);
        CountDownLatch transactionLatch = new CountDownLatch(count);
        Collector collector = new Collector();
        collector.setTotal(count);

        for (int i = 0; i < sendContractCount; ++i) {
            final int index = i % contracts.size();
            Account account = contracts.get(index);
            List<CryptoKeyPair> cryptoKeyPairs = contractAclMap.get(account.getContractAddress());
            assert cryptoKeyPairs.size() == DEFAULT_ADDRESS_COUNT;
            for (CryptoKeyPair cryptoKeyPair : cryptoKeyPairs) {
                limiter.acquire();
                // TODO: check this client set will access Account
                client.getCryptoSuite().setCryptoKeyPair(cryptoKeyPair);
                threadPoolService
                        .getThreadPool()
                        .execute(
                                () -> {
                                    long now = System.currentTimeMillis();

                                    final long value = Math.abs(random.nextLong() % 1000);

                                    account.addBalance(
                                            BigInteger.valueOf(value),
                                            new TransactionCallback() {
                                                @Override
                                                public void onResponse(TransactionReceipt receipt) {
                                                    long cost = System.currentTimeMillis() - now;
                                                    collector.onMessage(receipt, cost);
                                                    if (receipt.isStatusOK()) {
                                                        AtomicLong count1 =
                                                                accountLedger.get(index);
                                                        count1.addAndGet(value);
                                                    }

                                                    receivedBar.step();
                                                    transactionLatch.countDown();
                                                }
                                            });
                                    sentBar.step();
                                });
            }
        }
        transactionLatch.await();

        sentBar.close();
        receivedBar.close();
        collector.report();

        System.out.println("Sending transactions finished!");

        System.out.println("Checking result...");
        CountDownLatch checkLatch = new CountDownLatch(count);
        for (Map.Entry<Integer, AtomicLong> entry : accountLedger.entrySet()) {
            limiter.acquire();
            final int index = entry.getKey();
            final long expectBalance = entry.getValue().longValue();
            threadPoolService
                    .getThreadPool()
                    .execute(
                            () -> {
                                try {
                                    limiter.acquire();
                                    BigInteger balance = contracts.get(index).balance();
                                    if (balance.longValue() != expectBalance) {
                                        System.out.println(
                                                "Check failed! Account["
                                                        + index
                                                        + "] balance: "
                                                        + balance
                                                        + " not equal to expected: "
                                                        + expectBalance);
                                    }

                                    checkLatch.countDown();
                                } catch (ContractException e) {
                                    e.printStackTrace();
                                }
                            });
        }
        System.out.println("Checking finished!");
    }
}
