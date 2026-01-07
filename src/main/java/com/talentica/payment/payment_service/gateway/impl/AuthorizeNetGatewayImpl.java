package com.talentica.payment.payment_service.gateway.impl;

import com.talentica.payment.payment_service.config.AuthorizeNetProperties;
import com.talentica.payment.payment_service.domain.entity.PaymentOrder;
import com.talentica.payment.payment_service.gateway.AuthorizeNetGateway;
import com.talentica.payment.payment_service.gateway.dto.GatewayResponse;
import lombok.RequiredArgsConstructor;
import net.authorize.Environment;
import net.authorize.api.contract.v1.*;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class AuthorizeNetGatewayImpl implements AuthorizeNetGateway {

    private final AuthorizeNetProperties properties;

    private void setupMerchantAuth() {
        ApiOperationBase.setEnvironment(
                properties.isSandbox() ? Environment.SANDBOX : Environment.PRODUCTION
        );

        MerchantAuthenticationType auth = new MerchantAuthenticationType();
        auth.setName(properties.getApiLoginId());
        auth.setTransactionKey(properties.getTransactionKey());

        ApiOperationBase.setMerchantAuthentication(auth);
    }

    @Override
    public GatewayResponse purchase(PaymentOrder order) {
        return executeTransaction(order, TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION);
    }

    @Override
    public GatewayResponse authorize(PaymentOrder order) {
        return executeTransaction(order, TransactionTypeEnum.AUTH_ONLY_TRANSACTION);
    }

    @Override
    public GatewayResponse capture(PaymentOrder order, String refTransactionId) {

        setupMerchantAuth();

        TransactionRequestType request = new TransactionRequestType();
        request.setTransactionType(
                TransactionTypeEnum.PRIOR_AUTH_CAPTURE_TRANSACTION.value()
        );
        request.setRefTransId(refTransactionId);

        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(request);

        CreateTransactionController controller =
                new CreateTransactionController(apiRequest);
        controller.execute();

        return parseResponse(controller.getApiResponse());
    }

    @Override
    public GatewayResponse cancel(PaymentOrder order, String refTransactionId) {

        setupMerchantAuth();

        TransactionRequestType request = new TransactionRequestType();
        request.setTransactionType(
                TransactionTypeEnum.VOID_TRANSACTION.value()
        );
        request.setRefTransId(refTransactionId);

        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(request);

        CreateTransactionController controller =
                new CreateTransactionController(apiRequest);
        controller.execute();

        return parseResponse(controller.getApiResponse());
    }


    @Override
    public GatewayResponse refund(PaymentOrder order,
                                  String refTransactionId,
                                  BigDecimal amount) {

        setupMerchantAuth();

        TransactionRequestType request = new TransactionRequestType();
        request.setTransactionType(TransactionTypeEnum.REFUND_TRANSACTION.value());
        request.setAmount(amount);
        request.setRefTransId(refTransactionId);

        CreditCardType card = new CreditCardType();
        card.setCardNumber("1111");
        card.setExpirationDate("XXXX");

        PaymentType payment = new PaymentType();
        payment.setCreditCard(card);
        request.setPayment(payment);

        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(request);

        CreateTransactionController controller =
                new CreateTransactionController(apiRequest);
        controller.execute();

        return parseResponse(controller.getApiResponse());
    }

    private GatewayResponse executeTransaction(PaymentOrder order,
                                               TransactionTypeEnum type) {
        try {
            setupMerchantAuth();

            TransactionRequestType request = new TransactionRequestType();
            request.setTransactionType(type.value());
            request.setAmount(order.getAmount());

            PaymentType payment = new PaymentType();
            CreditCardType card = new CreditCardType();
            card.setCardNumber("4111111111111111"); // sandbox test card
            card.setExpirationDate("2038-12");
            payment.setCreditCard(card);

            request.setPayment(payment);

            CreateTransactionRequest apiRequest = new CreateTransactionRequest();
            apiRequest.setTransactionRequest(request);

            CreateTransactionController controller =
                    new CreateTransactionController(apiRequest);
            controller.execute();

            CreateTransactionResponse response = controller.getApiResponse();

            return parseResponse(response);

        } catch (Exception e) {
            return new GatewayResponse(false, null, e.getMessage());
        }
    }

    private GatewayResponse parseResponse(CreateTransactionResponse response) {

        if (response == null || response.getMessages().getResultCode() != MessageTypeEnum.OK) {
            String error =
                    response != null && response.getMessages() != null
                            ? response.getMessages().getMessage().get(0).getText()
                            : "Unknown error";
            return new GatewayResponse(false, null, error);
        }

        TransactionResponse result = response.getTransactionResponse();

        if (result != null && result.getResponseCode().equals("1")) {
            return new GatewayResponse(
                    true,
                    result.getTransId(),
                    null
            );
        }

        return new GatewayResponse(false, null,
                result != null && result.getErrors() != null
                        ? result.getErrors().getError().get(0).getErrorText()
                        : "Transaction failed");
    }

}
