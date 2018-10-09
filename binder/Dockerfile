FROM gradle:4.8.1-jdk10 as kernel-builder

USER root

# Install the kernel
RUN curl -L https://github.com/SpencerPark/IJava/archive/v1.1.2.tar.gz > v1.1.2.tar.gz \
  && tar xf v1.1.2.tar.gz

COPY ./binder/configure-ijava-install.gradle /configure-ijava-install.gradle

RUN cd IJava-1.1.2/ \
  && gradle zipKernel -I /configure-ijava-install.gradle \
  && cp build/distributions/ijava-kernel.zip /ijava-kernel.zip


FROM openjdk:10.0.1-10-jdk

ENV NB_USER jovyan
ENV NB_UID 1000
ENV HOME /home/$NB_USER

RUN adduser --disabled-password \
    --gecos "Default user" \
    --uid $NB_UID \
    $NB_USER

RUN apt-get update
RUN apt-get install -y python3-pip

RUN pip3 install --no-cache-dir notebook==5.5.* jupyterlab==0.32.*

COPY --from=kernel-builder /ijava-kernel.zip ijava-kernel.zip

RUN unzip ijava-kernel.zip -d ijava-kernel \
  && cd ijava-kernel \
  && python3 install.py --sys-prefix

COPY . $HOME
RUN chown -R $NB_UID $HOME

USER $NB_USER

WORKDIR $HOME
CMD ["jupyter", "notebook", "--ip", "0.0.0.0"]
