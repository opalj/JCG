package parser;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ExprParserFactory {

    public static IExpressionParser create() {
        ServiceLoader<IExpressionParser> sl = ServiceLoader.load(IExpressionParser.class);
        Iterator<IExpressionParser> iter = sl.iterator();
        if (!iter.hasNext()) {
            throw new RuntimeException("No service providers found!");
        }

        IExpressionParser parser = null;
        while (iter.hasNext()) {
            parser = iter.next();
            System.out.println("Found " + parser.parsingNotation() + " parser.");
        }

        return parser;
    }
}