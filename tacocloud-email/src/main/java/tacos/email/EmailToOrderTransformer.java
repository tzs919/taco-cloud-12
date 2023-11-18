package tacos.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.mail.transformer.AbstractMailMessageTransformer;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EmailToOrderTransformer
        extends AbstractMailMessageTransformer<EmailOrder> {

    private static Logger log =
            LoggerFactory.getLogger(EmailToOrderTransformer.class);

    private static final String SUBJECT_KEYWORDS = "TACO ORDER";
    static  private  Map<String, Ingredient> map = new HashMap<String, Ingredient>();

    static {
        map.put("flourTortilla", new Ingredient("FLTO", "Flour Tortilla", Ingredient.Type.WRAP));
        map.put("cornTortilla", new Ingredient("COTO", "Corn Tortilla", Ingredient.Type.WRAP));
        map.put("groundBeef", new Ingredient("GRBF", "Ground Beef", Ingredient.Type.PROTEIN));
        map.put("carnitas", new Ingredient("CARN", "Carnitas", Ingredient.Type.PROTEIN));
        map.put("tomatoes", new Ingredient("TMTO", "Diced Tomatoes", Ingredient.Type.VEGGIES));
        map.put("lettuce", new Ingredient("LETC", "Lettuce", Ingredient.Type.VEGGIES));
        map.put("cheddar", new Ingredient("CHED", "Cheddar", Ingredient.Type.CHEESE));
        map.put("jack", new Ingredient("JACK", "Monterrey Jack", Ingredient.Type.CHEESE));
        map.put("salsa", new Ingredient("SLSA", "Salsa", Ingredient.Type.SAUCE));
        map.put("sourCream", new Ingredient("SRCR", "Sour Cream", Ingredient.Type.SAUCE));
    }
    @Override
    protected AbstractIntegrationMessageBuilder<EmailOrder>
    doTransform(Message mailMessage) throws Exception {
        EmailOrder tacoOrder = processPayload(mailMessage);
        return MessageBuilder.withPayload(tacoOrder);
    }

    private EmailOrder processPayload(Message mailMessage) {
        try {
            String subject = mailMessage.getSubject();
            if (subject.toUpperCase().contains(SUBJECT_KEYWORDS)) {
                String email =
                        ((InternetAddress) mailMessage.getFrom()[0]).getAddress();
                byte[] bytes = new byte[100];
                mailMessage.getInputStream().read(bytes);
                String content = new String(bytes);
                return parseEmailToOrder(email, content);
            }
        } catch (MessagingException e) {
            log.error("MessagingException: {}", e);
        } catch (IOException e) {
            log.error("IOException: {}", e);
        }
        return null;
    }

    private EmailOrder parseEmailToOrder(String email, String content) {
        EmailOrder order = new EmailOrder(email);
        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            if (line.trim().length() > 0 && line.contains(":")) {
                String[] lineSplit = line.split(":");
                String tacoName = lineSplit[0].trim();
                String ingredients = lineSplit[1].trim();
                String[] ingredientsSplit = ingredients.split(",");
                List<Ingredient> ingredientCodes = new ArrayList<>();
                for (String ingredientName : ingredientsSplit) {
                    String code = ingredientName.trim();
                    if (code != null) {
                        ingredientCodes.add(map.get(code));
                    }
                }

                Taco taco = new Taco(tacoName);
                taco.setIngredients(ingredientCodes);
                order.addTaco(taco);
            }
        }
        return order;
    }
}
