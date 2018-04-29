package com.steve.netclient;

/**
 * Classe qui gère les differentes exceptions réseaux d'Adwa
 *
 * @author Steve Tchatchouang
 */
public class NetError extends Exception {
    private Throwable e;
    private ErrorType errorType;

    /**
     * Construire l'objet à partir d'une autre exception, dans le but de connaitre d'avance le message correspondant à l'erreur
     *
     * @param e exception
     */
    protected NetError(Throwable e) {
        this.e = e;
        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("socket")
                || e.getMessage().toLowerCase().contains("connection"))
            this.errorType = ErrorType.CONNECTION_ERROR;
        else
            this.errorType = ErrorType.EXCEPTION_ERROR;
    }

    /**
     * Construire l'objet à partir d'un type connu {@link ErrorType}
     *
     * @param errorType type d'erreur
     */
    protected NetError(ErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * Permet d'obtenir une chaine de caractère internationalisée, décrivant l'exception
     * <p>NB : la chaine est traduite automatiquement en anglais si la langue du système est inconnue</p>
     *
     * @return resource chaine de caractère internationalisée
     */
    public int getI18NMessage() {
        switch (this.errorType) {
            case EXCEPTION_ERROR:
                return R.string.error_occur;
            case CONNECTION_ERROR:
                if (e != null && e.getMessage().toLowerCase().contains("time") &&
                        e.getMessage().toLowerCase().contains("out")) {
                    return R.string.time_out_retry;
                }
                return R.string.connection_problem;
            case RETURN_FALSE_ERROR:
                return R.string.empty_result;
            case SERVER_ERROR:
                return R.string.server_problem_desc;
            default:
                return R.string.oops_desc;
        }
    }

    /**
     * Retourne le type d'erreur
     *
     * @return {@link ErrorType} correspondant à l'exception
     */
    public ErrorType getErrorType() {
        return errorType;
    }

    /**
     * Enumeration indiquant le type d'erreur en cas d'exception
     */
    public enum ErrorType {
        /**
         * Erreur de connexion
         */
        CONNECTION_ERROR,
        /**
         * Le serveur retourne false dans son web service
         */
        RETURN_FALSE_ERROR,
        /**
         * Une exception s'est produite pendant le processus
         */
        EXCEPTION_ERROR,
        /**
         * Erreur du serveur : 500,501 etc...
         */
        SERVER_ERROR
    }
}
