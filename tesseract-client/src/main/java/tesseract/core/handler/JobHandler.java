package tesseract.core.handler;

import tesseract.core.context.ExecutorContext;

@FunctionalInterface
public interface JobHandler {
    void execute(ExecutorContext executorContext) throws Exception;
}
