package com.yirendai.oss.lib.common.msginterpolate;

import org.springframework.beans.factory.Aware;

/**
 * Interface to be implemented by any object that wishes to be notified of the
 * {@link MessageInterpolator} to use.
 */
public interface MessageInterpolatorAware extends Aware {

  void setMessageInterpolator(MessageInterpolator messageInterpolator);
}
