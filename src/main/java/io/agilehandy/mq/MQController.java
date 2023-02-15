package io.agilehandy.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.agilehandy.lib.commons.TaxType;
import io.agilehandy.lib.model.TaxPayerInfo;
import io.agilehandy.lib.model.formET706.DocInfo;
import io.agilehandy.lib.model.formET706.ReturnInfo;
import io.agilehandy.lib.model.formET706.TaxFormET706;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

@RestController
@Slf4j
public class MQController {

    @Value("${ibm.mq.queueName}")
    private String queueName;

    private final JmsTemplate jmsTemplate;

    public MQController(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @PostMapping(path = "send", consumes = {MediaType.APPLICATION_XML_VALUE})
    public void send(@RequestBody String xml){
        log.info("sending to MQ {}", xml);
        try{
            jmsTemplate.convertAndSend(queueName, xml);
        }catch(JmsException ex){
            ex.printStackTrace();
        }
    }

    @GetMapping("sendtest")
    public String sendtest(){
        try{
            jmsTemplate.convertAndSend(queueName, "Hello World!");
            return "OK";
        }catch(JmsException ex){
            ex.printStackTrace();
            return "FAIL";
        }
    }

    @GetMapping(path = "form")
    public String getDummyform() throws JsonProcessingException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        DocInfo docInfo = new DocInfo();
        docInfo.setDocumentId("RG9999");
        docInfo.setAdvPayment(12.4);
        docInfo.setBalDueAmt(178.2);
        docInfo.setDeceasedDt(sdf.format(new Date()));
        docInfo.setLeinInd("Y");
        docInfo.setDeathCertInd("Y");
        docInfo.setFedTxbleEstateAmt(2000.50);
        //docInfo.setFedRtnInd("Y");
        docInfo.setPaymentAmt(2000.5);
        docInfo.setDeathCertInd("Y");
        docInfo.setReceiveDt(sdf.format(new Date()));
        docInfo.setTotalPaymentAmt(3400.6);

        ReturnInfo returnInfo = new ReturnInfo();
        returnInfo.setDocInfo(docInfo);

        TaxPayerInfo tpInfo = new TaxPayerInfo();
        tpInfo.setTpId(12345678L);
        tpInfo.setFormId(706);
        tpInfo.setProcessYear(2023);
        tpInfo.setTaxType(TaxType.ET.name());
        tpInfo.setSourceCd("paper");

        TaxFormET706 form = new TaxFormET706();
        form.setReturnInfo(returnInfo);
        form.setTaxpayerInfo(tpInfo);

        XmlMapper xmlMapper = new XmlMapper();
        return xmlMapper.writeValueAsString(form);
    }

}
