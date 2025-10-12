import { ElMessage } from 'element-plus';

export interface TTSSettings {
  lang?: string;     // 默认 'en-US'
  rate?: number;     // 语速 0.5 ~ 2.0
  pitch?: number;    // 音调 0 ~ 2
  volume?: number;   // 音量 0 ~ 1
}

// 听写模式配置接口
export interface DictationSettings extends TTSSettings {
  pauseBetweenWords?: number;  // 单词间停顿时间(毫秒)
  repeatCount?: number;        // 每个单词重复次数
  showSpelling?: boolean;      // 是否显示拼写
}

class TTSManager {
  private isSpeaking = false;
  private currentUtterance: SpeechSynthesisUtterance | null = null;
  private currentVoice: SpeechSynthesisVoice | null = null;
  private voicesLoaded = false;
  private voicesLoadingCallback: (() => void)[] = [];

  constructor() {
    // 初始化时检查语音是否已加载
    if ('speechSynthesis' in window && speechSynthesis.getVoices().length > 0) {
      this.voicesLoaded = true;
    }
    
    // 监听语音加载完成事件
    if ('speechSynthesis' in window) {
      window.speechSynthesis.onvoiceschanged = () => {
        this.voicesLoaded = true;
        // 触发所有等待的回调
        this.voicesLoadingCallback.forEach(callback => callback());
        this.voicesLoadingCallback = [];
      };
    }
  }

  // 确保语音加载完成
  private ensureVoicesLoaded(): Promise<void> {
    return new Promise((resolve) => {
      if (this.voicesLoaded) {
        resolve();
      } else {
        this.voicesLoadingCallback.push(resolve);
        // 强制触发voiceschanged事件
        window.speechSynthesis.getVoices();
        
        // 设置超时，避免无限等待
        setTimeout(() => {
          if (!this.voicesLoaded) {
            this.voicesLoaded = true; // 超时后认为已加载（使用默认语音）
            resolve();
          }
        }, 2000);
      }
    });
  }

  // 获取可用的英文语音列表
  getVoices(): SpeechSynthesisVoice[] {
    if (!this.isSupported()) return [];
    return speechSynthesis.getVoices().filter(v => v.lang.startsWith('en'));
  }

  // 设置语音
  setVoice(voice: SpeechSynthesisVoice): void {
    this.currentVoice = voice;
  }

  // 发音单个单词
  async speakWord(word: string, settings: TTSSettings = {}) {
    if (!this.isSupported()) return;

    // 确保语音已加载
    await this.ensureVoicesLoaded();

    // 停止当前正在播放的语音
    speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(word);
    utterance.lang = settings.lang ?? 'en-US';
    utterance.rate = Math.min(2, Math.max(0.5, settings.rate ?? 1.1));
    utterance.pitch = settings.pitch ?? 1;
    utterance.volume = settings.volume ?? 1;

    // 尝试使用英文语音（提升发音质量）
    const voices = speechSynthesis.getVoices();
    const enVoice = voices.find(v =>
      v.lang.startsWith('en') && (v.name.includes('Google') || v.name.includes('US'))
    );
    if (enVoice) utterance.voice = enVoice;


    utterance.onstart = () => {
      this.isSpeaking = true;
    };

    utterance.onend = () => {
      this.isSpeaking = false;
      this.currentUtterance = null;
    };

    utterance.onerror = (e: SpeechSynthesisErrorEvent) => {
      // 只记录非中断类型的错误，主动中断是正常行为
      if (e.error !== 'interrupted') {
        console.error('TTS 发音错误:', e);
      }
      this.isSpeaking = false;
    };

    speechSynthesis.speak(utterance);
    this.currentUtterance = utterance;
  }

  // 分段朗读：支持回调
  async speakParagraphs(
    paragraphs: string[],
    options: { rate?: number; onHighlight?: (index: number) => void; onFinish?: () => void }
  ): Promise<void> {
    if (!this.isSupported()) return;

    // 确保语音已加载
    await this.ensureVoicesLoaded();

    speechSynthesis.cancel();
    this.isSpeaking = true;


    const { rate = 1.0, onHighlight, onFinish } = options;

    const speakNext = (index: number) => {
      if (index >= paragraphs.length) {
        this.isSpeaking = false;
        onFinish?.();
        return;
      }

      const text = paragraphs[index].trim();
      if (!text) {
        speakNext(index + 1);
        return;
      }

      const utterance = new SpeechSynthesisUtterance(text);
      utterance.rate = rate;
      utterance.voice = this.currentVoice || null;

      // 高亮当前段落
      utterance.onstart = () => {
        onHighlight?.(index);
      };

      // 段落结束，继续下一段
      utterance.onend = () => {
        setTimeout(() => speakNext(index + 1), 300); // 段落间停顿
      };

      utterance.onerror = (e: SpeechSynthesisErrorEvent) => {
        // 只记录非中断类型的错误，主动中断是正常行为
        if (e.error !== 'interrupted') {
          console.error('TTS Error:', e);
          ElMessage.error('朗读出错，请重试');
        }
        this.isSpeaking = false;
      };

      speechSynthesis.speak(utterance);
      this.currentUtterance = utterance;
    };

    speakNext(0);
  }

  // 朗读整篇文章（自动分段防卡顿）
  speakArticle(text: string, settings: TTSSettings = {}) {
    // 将文本按段落分割并调用新的speakParagraphs方法
    const paragraphs = text.split(/\n{2,}/).filter(p => p.trim());
    if (paragraphs.length === 0) {
      // 如果没有段落标记，就作为一个段落处理
      this.speakParagraphs([text], { rate: settings.rate ?? 0.9 });
    } else {
      this.speakParagraphs(paragraphs, { rate: settings.rate ?? 0.9 });
    }
  }

  // 控制方法
  pause() {
    if (speechSynthesis) speechSynthesis.pause();
  }

  resume() {
    if (speechSynthesis) speechSynthesis.resume();
  }

  stop() {
    if (speechSynthesis) {
      speechSynthesis.cancel();
      this.isSpeaking = false;
      this.currentUtterance = null;
    }
  }

  isSupported(): boolean {
    return 'speechSynthesis' in window;
  }

  isBusy(): boolean {
    // 确保在所有浏览器中都能准确检测TTS状态
    try {
      return this.isSpeaking || (window.speechSynthesis && speechSynthesis.speaking);
    } catch (e) {
      console.warn('Error checking TTS status:', e);
      return this.isSpeaking;
    }
  }
  
  // 听写专用发音（带提示音和特殊配置）
  async speakForDictation(word: string, settings: DictationSettings = {}): Promise<void> {
    if (!this.isSupported()) {
      ElMessage.warning('您的浏览器不支持语音合成功能');
      return;
    }

    console.log('Starting dictation for word:', word);

    // 确保语音已加载
    await this.ensureVoicesLoaded();

    // 停止当前正在播放的语音
    speechSynthesis.cancel();

    // 构建带提示的文本
    const promptText = settings.showSpelling ? `Spell: ${word}` : word;
    
    // 使用较慢的语速以提高可辨识度
    const utterance = new SpeechSynthesisUtterance(promptText);
    utterance.lang = settings.lang ?? 'en-US';
    utterance.rate = Math.min(1.5, Math.max(0.7, settings.rate ?? 0.8)); // 比普通发音更慢
    utterance.pitch = settings.pitch ?? 1.1; // 稍微提高音调以增强区分度
    utterance.volume = settings.volume ?? 1;

    // 尝试使用英文语音
    const voices = speechSynthesis.getVoices();
    console.log('Available voices:', voices.length);
    
    // 增加语音选择的健壮性
    const enVoice = voices.find(v =>
      v.lang.startsWith('en') && (v.name.includes('Google') || v.name.includes('US') || v.lang.includes('US'))
    );
    
    if (enVoice) {
      utterance.voice = enVoice;
      console.log('Using voice:', enVoice.name);
    } else if (voices.length > 0) {
      // 如果找不到英文语音，至少使用第一个可用语音
      utterance.voice = voices[0];
      console.log('Using first available voice:', voices[0].name);
    }

    // 返回Promise以便调用者能够正确等待发音完成
    return new Promise((resolve, reject) => {
      // 添加一个超时机制，确保Promise不会无限等待
      const timeoutId = setTimeout(() => {
        console.warn('TTS timeout, forcing resolution');
        this.isSpeaking = false;
        resolve();
      }, 8000); // 8秒超时

      utterance.onstart = () => {
        console.log('TTS started speaking');
        this.isSpeaking = true;
      };

      utterance.onend = () => {
        clearTimeout(timeoutId);
        console.log('TTS finished speaking');
        this.isSpeaking = false;
        this.currentUtterance = null;
        resolve();
      };

      utterance.onerror = (e: SpeechSynthesisErrorEvent) => {
        clearTimeout(timeoutId);
        console.error('TTS 听写发音错误:', e.error, e);
        if (e.error !== 'interrupted') {
          ElMessage.error('发音失败，请重试');
        }
        this.isSpeaking = false;
        reject(e); // 在错误时拒绝Promise，而不是解析
      };

      try {
        speechSynthesis.speak(utterance);
        this.currentUtterance = utterance;
        console.log('Speech synthesis requested');
      } catch (err) {
        clearTimeout(timeoutId);
        console.error('Failed to initiate speech synthesis:', err);
        this.isSpeaking = false;
        ElMessage.error('无法开始发音，请重试');
        reject(err);
      }
    });
  }
  
  // 兼容旧版API的空init方法
  init(): void {
    // 空实现，保持向后兼容性
  }
}

// 单例导出
export const tts = new TTSManager();