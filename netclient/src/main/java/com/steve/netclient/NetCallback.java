package com.steve.netclient;

/**
 * Interface des requêtes asynchrones
 * <p>Cette interface est générique et attend le Type de du param value sous forme générique</p>
 * @author Steve Tchatchouang
 */
public interface NetCallback<T> {
    /**
     * Si tout s'est bien passé, cette methode s'executera
     * @param value valeur de la clé value que retourne le serveur
     * <p>NB :  c paramètre doit être converti(casting) avant d'être utilisé.</p>
     * <p>Ce qui entrainne que le type de réponse doit être connu d'avance</p>
     */
    void onSuccess(T value);

    /**
     * Si une erreur eventuelle se produit, cette methode s'executera
     * @param error objet encapsulant les informations sur l'erreur
     * @see NetError
     */
    void onError(NetError error);
}
