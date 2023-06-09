package se.ifmo.lab08.server.command;

import se.ifmo.lab08.common.dto.StatusCode;
import se.ifmo.lab08.common.dto.request.CommandRequest;
import se.ifmo.lab08.common.dto.response.CommandResponse;
import se.ifmo.lab08.common.dto.response.FlatCollectionResponse;
import se.ifmo.lab08.common.dto.response.Response;
import se.ifmo.lab08.common.exception.InvalidArgsException;
import se.ifmo.lab08.server.manager.CommandManager;

import java.util.ArrayList;

public class ShuffleCommand extends Command {
    public ShuffleCommand(CommandManager invoker) {
        super("shuffle", "перемешать элементы коллекции в случайном порядке", invoker);
    }

    @Override
    public Response execute(CommandRequest request) throws InvalidArgsException {
        validateArgs(request);
        invoker.getCollection().shuffle();
        var collection = new ArrayList<>(invoker.getCollection().getCollection());
        invoker.getServer().broadcastResponse(new FlatCollectionResponse(collection));
        return new CommandResponse("Collection has been shuffled.", StatusCode.OK, request.credentials());
    }
}
