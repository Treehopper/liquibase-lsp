FROM gitpod/workspace-full
USER gitpod
RUN sudo apt-get update -q && \
    sudo apt-get install -yq git-flow

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh \
             && sdk install java 16.0.1.hs-adpt \
             && sdk install mvnd 0.4.3"
