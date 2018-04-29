package com.steve.netclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Classe utile pour la gestion efficace de la mémoire et de la batterie de l'utilisateur
 * <p>Elle est chargée d'executer les appels réseaux dans une ou plusieurs files de type FIFO, selon les paramètres définis</p>
 *
 * @author Steve Tchatchouang
 */

public class NetQueue {

    private ExecutorService executorService;

    NetQueue() {
        this(1);
    }

    private NetQueue(int consumersNumbers) {
        executorService = new ThreadPoolExecutor(
                consumersNumbers,
                consumersNumbers,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>());
        NetLog.m("Queue instanciée");
    }

    /**
     * Ajoute un {@link NetClient} à la queue pour une exécution future
     *
     * @param netClient client
     * @return un objet Future
     */
    public Future<NetBody> addToQueue(final NetClient netClient) {
        NetLog.m("Client ajouté à la queue");
        return executorService.submit(netClient);
    }
}
