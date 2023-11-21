/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.sdk.demo.perf;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.fisco.bcos.sdk.v3.model.JsonRpcResponse;
import org.fisco.bcos.sdk.v3.model.PrecompiledRetCode;
import org.fisco.bcos.sdk.v3.model.RetCode;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.TransactionReceiptStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author monan */
public class Collector {
    private static Logger logger = LoggerFactory.getLogger(Collector.class);
    private AtomicLong less50 = new AtomicLong(0);
    private AtomicLong less100 = new AtomicLong(0);
    private AtomicLong less200 = new AtomicLong(0);
    private AtomicLong less400 = new AtomicLong(0);
    private AtomicLong less1000 = new AtomicLong(0);
    private AtomicLong less2000 = new AtomicLong(0);
    private AtomicLong timeout2000 = new AtomicLong(0);
    private AtomicLong totalCost = new AtomicLong(0);

    private Integer total = 0;
    private AtomicInteger received = new AtomicInteger(0);

    private AtomicInteger error = new AtomicInteger(0);
    private Long startTimestamp = System.currentTimeMillis();
    private Long sendFinishedTimestamp = 0L;
    private Long firstReceiptTimestamp = System.currentTimeMillis();
    private Long lastReceiptTimestamp = System.currentTimeMillis();

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getReceived() {
        return received.get();
    }

    public void setReceived(Integer received) {
        this.received.getAndSet(received);
    }

    public AtomicInteger getError() {
        return error;
    }

    public void onRpcMessage(JsonRpcResponse<?> response, Long cost) {
        try {
            boolean errorMessage = false;
            if (response.getError() != null && response.getError().getCode() != 0) {
                logger.warn("receive error jsonRpcResponse: {}", response);
                errorMessage = true;
            }
            stat(errorMessage, cost);
        } catch (Exception e) {
            logger.error("onRpcMessage exception: {}", e.getMessage());
        }
    }

    public void onMessage(TransactionReceipt receipt, Long cost) {
        try {
            boolean errorMessage = false;
            if (!receipt.isStatusOK()) {
                logger.error(
                        "error receipt, status: {}, output: {}, message: {}",
                        receipt.getStatus(),
                        receipt.getOutput(),
                        receipt.getMessage());
                errorMessage = true;
            }
            stat(errorMessage, cost);
        } catch (Exception e) {
            logger.error("error:", e);
        }
    }

    public void onPrecompiledMessage(RetCode retCode, Long cost) {
        try {
            boolean errorMessage = false;
            if (retCode.getCode() != PrecompiledRetCode.CODE_SUCCESS.code) {
                logger.error(
                        "error retCode, code: {}, message: {}", retCode.code, retCode.getMessage());
                errorMessage = true;
            }
            stat(errorMessage, cost);
        } catch (Exception e) {
            logger.error("error:", e);
        }
    }

    public void onAuthCheckMessage(TransactionReceipt receipt, Long cost) {
        try {
            boolean errorMessage = false;
            if (!receipt.isStatusOK()) {
                if (receipt.getStatus() == TransactionReceiptStatus.PermissionDenied.code) {
                    stat(false, cost);
                    return;
                }
                logger.error(
                        "error receipt, status: {}, output: {}, message: {}",
                        receipt.getStatus(),
                        receipt.getOutput(),
                        receipt.getMessage());
                errorMessage = true;
            }
            stat(errorMessage, cost);
        } catch (Exception e) {
            logger.error("error:", e);
        }
    }

    public void stat(boolean errorMessage, Long cost) {
        if (received.incrementAndGet() == 1) {
            firstReceiptTimestamp = System.currentTimeMillis();
        }
        if (received.get() == total) {
            lastReceiptTimestamp = System.currentTimeMillis();
        }
        if (errorMessage) {
            error.addAndGet(1);
        }

        if (cost < 50) {
            less50.incrementAndGet();
        } else if (cost < 100) {
            less100.incrementAndGet();
        } else if (cost < 200) {
            less200.incrementAndGet();
        } else if (cost < 400) {
            less400.incrementAndGet();
        } else if (cost < 1000) {
            less1000.incrementAndGet();
        } else if (cost < 2000) {
            less2000.incrementAndGet();
        } else {
            timeout2000.incrementAndGet();
        }

        totalCost.addAndGet(cost);
    }

    public void sendFinished() {
        sendFinishedTimestamp = System.currentTimeMillis();
    }

    public void report() {
        System.out.println("total");

        long totalTime = System.currentTimeMillis() - startTimestamp;

        System.out.println("===================================================================");

        System.out.println("Total transactions:  " + total);
        System.out.println("Total time: " + totalTime + "ms");
        long sendTime = sendFinishedTimestamp - startTimestamp;
        if (sendFinishedTimestamp != 0) {
            System.out.println(
                    "QPS                         : " + total / ((double) sendTime / 1000));
        }
        long receiptTime = lastReceiptTimestamp - firstReceiptTimestamp;
        System.out.println(
                "CTPS                        : " + total / ((double) receiptTime / 1000));

        System.out.println("TPS(include error requests): " + total / ((double) totalTime / 1000));

        System.out.println(
                "TPS(exclude error requests): "
                        + (total - error.get()) / ((double) totalTime / 1000));
        System.out.println("Avg time cost: " + totalCost.get() / total + "ms");
        System.out.println("Errors: " + error.get());

        System.out.println("Time group:");
        System.out.println(
                "0    < time <  50ms   : "
                        + less50
                        + "  : "
                        + (double) less50.get() / total * 100
                        + "%");
        System.out.println(
                "50   < time <  100ms  : "
                        + less100
                        + "  : "
                        + (double) less100.get() / total * 100
                        + "%");
        System.out.println(
                "100  < time <  200ms  : "
                        + less200
                        + "  : "
                        + (double) less200.get() / total * 100
                        + "%");
        System.out.println(
                "200  < time <  400ms  : "
                        + less400
                        + "  : "
                        + (double) less400.get() / total * 100
                        + "%");
        System.out.println(
                "400  < time <  1000ms : "
                        + less1000
                        + "  : "
                        + (double) less1000.get() / total * 100
                        + "%");
        System.out.println(
                "1000 < time <  2000ms : "
                        + less2000
                        + "  : "
                        + (double) less2000.get() / total * 100
                        + "%");
        System.out.println(
                "2000 < time           : "
                        + timeout2000
                        + "  : "
                        + (double) timeout2000.get() / total * 100
                        + "%");
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }
}
